package br.com.damatomos.create_short_url;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final S3Client s3Client = S3Client.builder().build();

    private final String bucketStorageName = "aws-damatomos-bucket-mark-one";

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        /*
        * O 'input' é a requisicao e nela temos os seguintes dados
        * {
        *   body: "dados do corpo do objeto",
        *   header: "informacoes de cabecalho"
        * }
        * */

        // Pega o body da requisicao
        String body = input.get("body").toString();


        // Transforma os dados do body que é um json como string para um HashMap
        Map<String, String> bodyMap;
        try
        {
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON body: " + e.getMessage(), e);
        }

        // Recupera os dados dentro do body
        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long expirationTimeInSeconds = Long.parseLong(expirationTime);

        // Gera um shortId
        String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        UrlData urlData = new UrlData(originalUrl,  expirationTimeInSeconds);

        // Transforma os dados do urlData em um Json
        // cria um arquivo json com o shortId gerado
        // salva os dados do urlDataJson no arquivo criado
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketStorageName)
                    .key(shortUrlCode + ".json").build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch(Exception e)
        {
            throw new RuntimeException("Error saving data to S3" + e.getMessage(), e);
        }

        // Cria um HashMap para a resposta e envia apenas o shortId
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", shortUrlCode);

        return response;
    }
}