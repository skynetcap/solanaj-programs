package com.mmorrell.raydium.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmorrell.raydium.model.LiquidityState;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.TokenResultObjects;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;

public class RaydiumLPManager {
    private static final Logger LOGGER = LogManager.getLogger(RaydiumLPManager.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final RpcClient client;

    public RaydiumLPManager(final RpcClient client) {
        this.client = client;
    }

    public boolean isLpLockedGivenTokenAddress(String tokenAddress) {
        String lpMarketAddress = getRaydiumLpMarketPublicKey(tokenAddress);
        return isLpLockedGivenLpMarketAddress(lpMarketAddress);
    }

    public boolean isLpLockedGivenLpMarketAddress(String lpMarketAddress) {
        BigDecimal burnPercent = checkRaydiumLpBurnPercentageGivenLpMarketAddress(lpMarketAddress);
        return burnPercent.compareTo(BigDecimal.valueOf(95)) > 0;
    }

    public BigDecimal checkRaydiumLpBurnPercentageGivenTokenAddress(String tokenAddress) {
        String lpMarketAddress = getRaydiumLpMarketPublicKey(tokenAddress);
        return checkRaydiumLpBurnPercentageGivenLpMarketAddress(lpMarketAddress);
    }

    public BigDecimal checkRaydiumLpBurnPercentageGivenLpMarketAddress(String lpMarketAddress) {
        PublicKey lpMarketPublicKey = new PublicKey(lpMarketAddress);
        AccountInfo lpMarketAddressInfo;
        try {
            lpMarketAddressInfo = client.getApi().getAccountInfo(lpMarketPublicKey);
        } catch (RpcException e) {
            LOGGER.error("RPC request issue getting lp market address account info: {}", lpMarketPublicKey);
            throw new RuntimeException(e);
        }

        assert lpMarketAddressInfo != null;
        LiquidityState liquidityState = LiquidityState.decode(lpMarketAddressInfo.getDecodedData());

        PublicKey lpMint = liquidityState.lpMint();
        BigInteger lpReserveBigInteger = liquidityState.u64LpReserve();
        BigDecimal lpReserve = new BigDecimal(lpReserveBigInteger);

        TokenResultObjects.TokenInfo info;
        try {
            info = client.getApi().getSplTokenAccountInfo(lpMint)
                    .getValue()
                    .getData()
                    .getParsed()
                    .getInfo();
        } catch (RpcException e) {
            LOGGER.error("Failed: Issue getting account info for lp mint: {}", lpMint);
            throw new RuntimeException(e);
        }

        Integer decimals = info.getDecimals();
        lpReserve = lpReserve.divide(BigDecimal.valueOf(10).pow(decimals), 2, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        BigDecimal actualSupply = new BigDecimal(info.getSupply()).divide(BigDecimal.valueOf(10).pow(decimals), 2, RoundingMode.HALF_UP);
        BigDecimal maxLpSupply = actualSupply.max(lpReserve);

        BigDecimal burnAmt = maxLpSupply.subtract(actualSupply);
        try {
            return burnAmt.divide(maxLpSupply, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        } catch (Exception e) {
            if (e.getMessage().equals("/ by zero")) {
                return BigDecimal.ZERO;
            }
            throw new RuntimeException(String.format("Failed to divide burnAmt %s by maxLpSupply %s: ", burnAmt, maxLpSupply), e);
        }
    }

    public String getRaydiumLpMarketPublicKey(String tokenAddress) {
        HttpGet tokenLpMarketGetHttpRequest = createGetHttpRequest(tokenAddress);
        JsonNode tokenInfoJsonNode;
        try {
            tokenInfoJsonNode = httpClient.execute(tokenLpMarketGetHttpRequest, getHttpRequestHandler());
        } catch (IOException e) {
            LOGGER.error("Failed: Issue getting from geckoTerminal for token: {}", tokenAddress);
            throw new RuntimeException(e);
        }
        return tokenInfoJsonNode.get("data")
                .get("relationships")
                .get("top_pools")
                .withArrayProperty("data")
                .get(0)
                .get("id")
                .asText()
                .replace("solana_", "");
    }

    public HttpGet createGetHttpRequest(String tokenAddress) {
        String endpoint = "https://api.geckoterminal.com/api/v2/networks/solana/tokens/" + tokenAddress;
        URI tokenInfoURI;
        try {
            tokenInfoURI = new URIBuilder(endpoint)
                    .build();
            LOGGER.info("Creating uri for GeckoTerminal endpoint: {}", endpoint);
        } catch (URISyntaxException e) {
            LOGGER.error("Failed: Issue creating URI from geckoTerminal for token: {}", tokenAddress);
            throw new RuntimeException("Failed to send get request to geckoTerminal, ", e);
        }

        HttpGet tokenInfoGetRequest = new HttpGet(tokenInfoURI);
        tokenInfoGetRequest.setHeader("accept", "application/json");

        return tokenInfoGetRequest;
    }

    private static HttpClientResponseHandler<JsonNode> getHttpRequestHandler() {
        return response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? objectMapper.readTree(EntityUtils.toString(entity)) : null;
            } else {
                LOGGER.error("Failed: Issue getting http request handler");
                throw new RuntimeException("Failed: Unexpected response status: " + status);
            }
        };
    }
}
