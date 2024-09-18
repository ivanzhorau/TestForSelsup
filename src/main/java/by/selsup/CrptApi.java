package by.selsup;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CrptApi {
    private final HttpClient httpClient;
    private final int requestLimit;
    private final long timeWindowInMillis;
    private final AtomicInteger currentRequestCount = new AtomicInteger(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());
    private final Semaphore semaphore;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Gson gson = new Gson();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeWindowInMillis = timeUnit.toMillis(1);
        this.httpClient = HttpClient.newBuilder().build();
        this.semaphore = new Semaphore(requestLimit);
    }

    // Метод для создания документа
    public HttpResponse<String> createDocument(Document document, String signature) throws Exception {
        // Конвертируем объект Document в JSON
        String jsonPayload = gson.toJson(document);

        return sendPostRequest("https://ismp.crpt.ru/api/v3/lk/documents/create", jsonPayload, signature);
    }

    private HttpResponse<String> sendPostRequest(String url, String jsonPayload, String signature) throws Exception {
        limitRate();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Метод, реализующий ограничение количества запросов
    private void limitRate() throws InterruptedException {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastResetTime.get() >= timeWindowInMillis) {
                lastResetTime.set(currentTime);
                currentRequestCount.set(0);
                semaphore.release(requestLimit - semaphore.availablePermits());
            }

            semaphore.acquire();
            currentRequestCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public static class Document {

        private Description description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private List<Product> products;
        private String regDate;
        private String regNumber;

        public Document(Description description, String docId, String docStatus, String docType, boolean importRequest, String ownerInn, String participantInn, String producerInn, String productionDate, String productionType, List<Product> products, String regDate, String regNumber) {
            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }
    }

    public static class Product {
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

        public Product(String certificateDocument, String certificateDocumentDate, String certificateDocumentNumber,
                       String ownerInn, String producerInn, String productionDate, String tnvedCode, String uitCode, String uituCode) {

            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
        }

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public String getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(String certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public void setTnvedCode(String tnvedCode) {
            this.tnvedCode = tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public void setUitCode(String uitCode) {
            this.uitCode = uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public void setUituCode(String uituCode) {
            this.uituCode = uituCode;
        }
    }

    public static class Description {
        private String participantInn;

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static void main(String[] args) throws Exception {

        // Создание объектов Product
        Product product1 = new Product("cert01", "2024-09-18", "123", "owner01", "producer01", "2024-09-18", "tnved01", "uit01", "uitu01");
        Product product2 = new Product("cert02", "2024-09-18", "456", "owner02", "producer02", "2024-09-18", "tnved02", "uit02", "uitu02");
        List<Product> products = List.of(product1, product2);

        // Создание объекта Document
        Description description = new Description("1235");
        Document document = new Document(description, "docId", "approved", "doc_type", true, "owner_inn", "participant_inn", "producer_inn", "2024-09-18", "type", products, "2024-09-18", "id123");
        String signature = "signature";

        // Пример вызова метода
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        crptApi.createDocument(document, signature);
    }
}

