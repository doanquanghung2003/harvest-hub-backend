package com.example.harvesthubbackend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@Configuration
// Scan toàn bộ base package để chắc chắn quét được Repository
@EnableMongoRepositories(basePackages = "com.example.harvesthubbackend")
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @PostConstruct
    public void init() {
        System.out.println("MongoConfig: Initializing MongoDB configuration");
        System.out.println("MongoConfig: MongoDB URI: " + mongoUri);

        // IMPORTANT: Nâng giới hạn số lượng file-part mà Tomcat cho phép parse
        // Một số phiên bản Tomcat/Embedded đọc giá trị này từ System properties rất sớm
        // nên cần thiết lập càng sớm càng tốt khi app khởi động (trước khi có request multipart)
        try {
            System.setProperty("org.apache.tomcat.util.http.fileupload.fileCountMax", "10000");
            // Một số bản dùng key của Apache Commons FileUpload
            System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "10000");
            // Thêm dư dả tham số để không bị chặn bởi số lượng parameter
            System.setProperty("server.tomcat.max-parameter-count", "100000");
            System.out.println("MongoConfig: Applied system properties for multipart fileCountMax and parameter limits");
        } catch (Exception e) {
            System.err.println("MongoConfig: Failed to apply multipart system properties: " + e.getMessage());
        }

        System.out.println("MongoConfig: MongoDB configuration initialized successfully");
    }
}
