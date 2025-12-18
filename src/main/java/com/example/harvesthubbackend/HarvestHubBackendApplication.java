package com.example.harvesthubbackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// Tomcat imports đã được xóa để tránh lỗi compilation
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Service.UserService;

@SpringBootApplication
@EnableScheduling
public class HarvestHubBackendApplication {

	public static void main(String[] args) {
		// Cấu hình JVM system properties để fix FileCountLimitExceededException
		System.setProperty("org.apache.tomcat.util.http.fileupload.fileCountMax", "1000");
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800"); // 50MB
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600"); // 100MB
		
		// Thêm cấu hình Tomcat multipart
		System.setProperty("server.tomcat.max-parameter-count", "1000");
		System.setProperty("server.tomcat.max-http-form-post-size", "104857600");
		System.setProperty("server.tomcat.max-swallow-size", "104857600");
		System.setProperty("server.tomcat.max-http-post-size", "104857600");
		
		// Override Apache Commons FileUpload hoàn toàn
		System.setProperty("org.apache.commons.fileupload.FileUploadBase.fileCountMax", "1000");
		System.setProperty("org.apache.commons.fileupload.FileUploadBase.fileSizeMax", "52428800");
		System.setProperty("org.apache.commons.fileupload.FileUploadBase.sizeMax", "104857600");
		
		// Override Spring Boot multipart settings
		System.setProperty("spring.servlet.multipart.file-size-threshold", "0");
		System.setProperty("spring.servlet.multipart.location", System.getProperty("java.io.tmpdir"));
		System.setProperty("spring.servlet.multipart.resolve-lazily", "false");
		
		// Override Tomcat multipart settings hoàn toàn
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800");
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600");
		
		// Log ra giá trị fileCountMax để xác nhận đã được áp dụng trước khi khởi chạy Spring
		System.out.println("fileCountMax=" + System.getProperty("org.apache.tomcat.util.http.fileupload.fileCountMax"));
		System.out.println("FileUploadBase.fileCountMax=" + System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax"));
		
		SpringApplication.run(HarvestHubBackendApplication.class, args);
	}

	// @Bean
	// public WebMvcConfigurer corsConfigurer() {
	// 	return new WebMvcConfigurer() {
	// 	@Override
	// 	public void addCorsMappings(@org.springframework.lang.NonNull CorsRegistry registry) {
	// 		registry.addMapping("/**")
	// 				.allowedOrigins("http://localhost:5173")
	// 				.allowedMethods("*");
	// 	}

	// 	@Override
	// 	public void addResourceHandlers(@org.springframework.lang.NonNull org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
	// 		// Phục vụ file tĩnh từ thư mục uploads (ngoài classpath)
	// 		registry.addResourceHandler("/uploads/**")
	// 				.addResourceLocations("file:uploads/");
	// 	}
	// 	};
	// }

	@Bean
	public MultipartConfigFactory multipartConfigFactory() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(50)); // 50MB mỗi file
		factory.setMaxRequestSize(DataSize.ofMegabytes(100)); // 100MB tổng request
		factory.setFileSizeThreshold(DataSize.ofBytes(0));
		
		// Override Apache Commons FileUpload settings - tăng giới hạn file count
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
		
		return factory;
	}

	// Tomcat customizer đã được xóa để tránh lỗi compilation
	// Cấu hình Tomcat được thực hiện qua application.properties
	
	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
		resolver.setResolveLazily(false);
		System.out.println("StandardServletMultipartResolver configured");
		return resolver;
	}
	
	@Bean
	public ServletContextInitializer servletContextInitializer() {
		return servletContext -> {
			// Cấu hình Apache Commons FileUpload - tăng giới hạn file count
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
			// QUAN TRỌNG: Đặt đúng key Tomcat đọc để giới hạn số file-part
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.fileCountMax", "1000");
			
			// Thêm cấu hình Tomcat multipart
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800"); // 50MB
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600"); // 100MB
			
			// Override Apache Commons FileUpload hoàn toàn
			servletContext.setAttribute("org.apache.commons.fileupload.FileUploadBase.fileCountMax", "1000");
			servletContext.setAttribute("org.apache.commons.fileupload.FileUploadBase.fileSizeMax", "52428800");
			servletContext.setAttribute("org.apache.commons.fileupload.FileUploadBase.sizeMax", "104857600");
			
			// Override Spring Boot multipart settings
			servletContext.setAttribute("spring.servlet.multipart.file-size-threshold", "0");
			servletContext.setAttribute("spring.servlet.multipart.location", System.getProperty("java.io.tmpdir"));
			servletContext.setAttribute("spring.servlet.multipart.resolve-lazily", "false");
			
			// Override Tomcat multipart settings trực tiếp
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "1000");
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800");
			servletContext.setAttribute("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600");
			
			System.out.println("ServletContext fileCountMax attribute set to 1000; FileUploadBase.fileCountMax set to 1000");
		};
	}

	@Bean
	CommandLineRunner seedData(UserService userService) {
		return args -> {
			// Seed an toàn: kiểm tra theo cả email và username để tránh crash do trùng lặp
			User existingByEmail = userService.getByEmail("admin@harvesthub.com");
			User existingByUsername = userService.getByUsername("admin");

			if (existingByEmail == null && existingByUsername == null) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("admin123");
				admin.setEmail("admin@harvesthub.com");
				admin.setRole("ADMIN");
				admin.setFirstName("Admin");
				admin.setLastName("User");
				admin.setPhoneNumber("0123456789");
				admin.setBio("Quản trị viên hệ thống Harvest Hub");
				admin.setMembershipType("VIP");
				admin.setMembershipDate("Tháng 8/2025");

				try {
					userService.create(admin);
					System.out.println("Admin user created: admin/admin123");
				} catch (RuntimeException ex) {
					System.out.println("Admin user seeding skipped: " + ex.getMessage());
				}
			} else {
				User target = existingByEmail != null ? existingByEmail : existingByUsername;
				User updateData = new User();
				updateData.setFirstName("Admin");
				updateData.setLastName("User");
				updateData.setPhoneNumber("0123456789");
				updateData.setBio("Quản trị viên hệ thống Harvest Hub");
				updateData.setMembershipType("VIP");
				updateData.setMembershipDate("Tháng 8/2025");
				updateData.setPassword("admin123");

				userService.update(target.getId(), updateData);
				System.out.println("Admin user ensured and updated. Password set to default.");
			}
		};
	}
}
