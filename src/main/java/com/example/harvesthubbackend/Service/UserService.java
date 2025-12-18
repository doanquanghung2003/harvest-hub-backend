package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Models.Seller;
import com.example.harvesthubbackend.Repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Repository
@Repository
interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

// Service
@Service
public class UserService implements UserDetailsService {
    private static final Set<String> ALLOWED_ACCOUNT_STATUSES = Set.of("ACTIVE", "VIOLATION", "RESTRICTED", "SUSPENDED");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return userRepository.findById(id).orElse(null);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User getByEmail(String email) {
        // Tránh lỗi non-unique khi dữ liệu email bị trùng: lấy bản ghi đầu tiên nếu có nhiều
        List<User> usersByEmail = userRepository.findAllByEmail(email);
        return usersByEmail.isEmpty() ? null : usersByEmail.get(0);
    }

    public User create(User user) {
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        // Mã hóa password và set role mặc định
        String encoded = passwordEncoder.encode(user.getPassword());
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        // Lưu lịch sử mật khẩu, bao gồm mật khẩu hiện tại
        user.addPasswordToHistory(encoded);
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        user.setAccountStatus(normalizeAccountStatus(user.getAccountStatus()));
        
        return userRepository.save(user);
    }

    public User update(String id, User user) {
        if (id == null || id.trim().isEmpty()) {
            throw new RuntimeException("User id is required");
        }
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }
        
        // Cập nhật thông tin cơ bản
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getAccountStatus() != null) {
            existingUser.setAccountStatus(normalizeAccountStatus(user.getAccountStatus()));
        } else if (existingUser.getAccountStatus() == null || existingUser.getAccountStatus().isEmpty()) {
            existingUser.setAccountStatus("ACTIVE");
        }
        
        // Cập nhật thông tin cá nhân
        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getMembershipType() != null) {
            existingUser.setMembershipType(user.getMembershipType());
        }
        if (user.getMembershipDate() != null) {
            existingUser.setMembershipDate(user.getMembershipDate());
        }
        
        // Cập nhật địa chỉ giao hàng
        if (user.getAddressStreet() != null) {
            existingUser.setAddressStreet(user.getAddressStreet());
        }
        if (user.getAddressWard() != null) {
            existingUser.setAddressWard(user.getAddressWard());
        }
        if (user.getAddressDistrict() != null) {
            existingUser.setAddressDistrict(user.getAddressDistrict());
        }
        if (user.getAddressCity() != null) {
            existingUser.setAddressCity(user.getAddressCity());
        }
        
        // Cập nhật payment PIN
        if (user.getPaymentPin() != null) {
            existingUser.setPaymentPin(user.getPaymentPin());
        }
        
        // Cập nhật account lockout fields
        if (user.getFailedLoginAttempts() >= 0) {
            existingUser.setFailedLoginAttempts(user.getFailedLoginAttempts());
        }
        if (user.getLockedUntil() != null || (user.getFailedLoginAttempts() == 0 && user.getLockedUntil() == null)) {
            existingUser.setLockedUntil(user.getLockedUntil());
        }
        if (user.isAccountNonLocked() != existingUser.isAccountNonLocked()) {
            existingUser.setAccountNonLocked(user.isAccountNonLocked());
        }
        
        // Cập nhật password nếu được cung cấp
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String incomingPassword = user.getPassword();
            String currentEncodedPassword = existingUser.getPassword();

            boolean isSameEncodedPassword = incomingPassword.equals(currentEncodedPassword);
            boolean isSameRawPassword = !isSameEncodedPassword && passwordEncoder.matches(incomingPassword, currentEncodedPassword);

            // Nếu dữ liệu đầu vào đã là mật khẩu được mã hóa (từ DB) hoặc là mật khẩu hiện tại thì bỏ qua
            if (!isSameEncodedPassword && !isSameRawPassword) {
                // Chặn trùng với bất kỳ mật khẩu trước đó
                List<String> history = existingUser.getPasswordHistory();
                if (history != null) {
                    for (String oldHash : history) {
                        if (passwordEncoder.matches(incomingPassword, oldHash)) {
                            throw new RuntimeException("New password must not match any previous passwords");
                        }
                    }
                }
                String newEncoded = passwordEncoder.encode(incomingPassword);
                existingUser.setPassword(newEncoded);
                existingUser.addPasswordToHistory(newEncoded);
                existingUser.setPasswordChangedAt(LocalDateTime.now());
                // Giới hạn kích thước lịch sử mật khẩu để tránh phình to (ví dụ: giữ 10 bản ghi gần nhất)
                List<String> updatedHistory = existingUser.getPasswordHistory();
                if (updatedHistory != null && updatedHistory.size() > 10) {
                    // Cắt bớt các bản ghi cũ
                    int start = updatedHistory.size() - 10;
                    existingUser.setPasswordHistory(new ArrayList<>(updatedHistory.subList(start, updatedHistory.size())));
                }
            }
        }
        
        if (existingUser.getAccountStatus() == null || existingUser.getAccountStatus().isEmpty()) {
            existingUser.setAccountStatus("ACTIVE");
        }
        
        return userRepository.save(existingUser);
    }

    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new RuntimeException("User id is required");
        }
        
        System.out.println("🗑️ Starting delete process for id: " + id);
        
        // Nếu ID bắt đầu bằng "seller_", đây là seller ID (từ frontend)
        // Cần bỏ prefix "seller_" để lấy seller ID thực
        if (id.startsWith("seller_")) {
            String sellerId = id.substring(7); // Bỏ "seller_" prefix (7 ký tự)
            if (sellerId == null || sellerId.trim().isEmpty()) {
                throw new RuntimeException("Invalid seller ID format: " + id);
            }
            System.out.println("🔍 Detected seller ID format. Extracted seller ID: " + sellerId);
            
            // Xóa seller trực tiếp
            if (sellerRepository.existsById(sellerId)) {
                System.out.println("🗑️ Deleting seller: " + sellerId);
                sellerRepository.deleteById(sellerId);
                
                // Verify seller was deleted
                boolean stillExists = sellerRepository.existsById(sellerId);
                if (stillExists) {
                    System.err.println("❌ ERROR: Seller " + sellerId + " still exists after delete attempt!");
                    throw new RuntimeException("Failed to delete seller: " + sellerId);
                } else {
                    System.out.println("✅ Successfully deleted seller: " + sellerId);
                }
            } else {
                System.out.println("⚠️ Seller " + sellerId + " does not exist, skipping delete");
                throw new RuntimeException("Seller not found: " + sellerId);
            }
            return; // Không xóa user nếu đây là seller
        }
        
        // Nếu ID không bắt đầu bằng "seller_", đây là user ID thực
        // Tìm seller liên quan (nếu có) bằng userId
        try {
            System.out.println("🔍 Searching for seller with userId: " + id);
            Optional<Seller> sellerOpt = sellerRepository.findByUserId(id);
            
            if (sellerOpt.isPresent()) {
                Seller seller = sellerOpt.get();
                String sellerId = seller.getId();
                
                if (sellerId != null && !sellerId.trim().isEmpty()) {
                    System.out.println("✅ Found seller: " + sellerId + " for user: " + id);
                    System.out.println("🗑️ Deleting seller: " + sellerId);
                    
                    // Xóa seller trước khi xóa user
                    sellerRepository.deleteById(sellerId);
                    
                    // Verify seller was deleted
                    boolean stillExists = sellerRepository.existsById(sellerId);
                    if (stillExists) {
                        System.err.println("❌ ERROR: Seller " + sellerId + " still exists after delete attempt!");
                        throw new RuntimeException("Failed to delete seller: " + sellerId);
                    } else {
                        System.out.println("✅ Successfully deleted seller: " + sellerId);
                    }
                } else {
                    System.out.println("⚠️ Seller found but sellerId is null or empty");
                }
            } else {
                System.out.println("ℹ️ No seller found for userId: " + id);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR deleting seller for user " + id + ": " + e.getMessage());
            e.printStackTrace();
            // Không tiếp tục xóa user nếu không xóa được seller
            throw new RuntimeException("Cannot delete user: failed to delete associated seller", e);
        }
        
        // Xóa user sau khi đã xóa seller thành công
        System.out.println("🗑️ Deleting user: " + id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            
            // Verify user was deleted
            boolean stillExists = userRepository.existsById(id);
            if (stillExists) {
                System.err.println("❌ ERROR: User " + id + " still exists after delete attempt!");
                throw new RuntimeException("Failed to delete user: " + id);
            } else {
                System.out.println("✅ Successfully deleted user: " + id);
            }
        } else {
            System.out.println("⚠️ User " + id + " does not exist, skipping delete");
        }
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    public void updateMembershipType(String userId, String membershipType) {
        if (userId == null || userId.trim().isEmpty() || membershipType == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        user.setMembershipType(membershipType);
        if (user.getMembershipDate() == null || user.getMembershipDate().trim().isEmpty()) {
            user.setMembershipDate(LocalDate.now().toString());
        }
        userRepository.save(user);
    }
    
    private String normalizeAccountStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }
        String normalized = status.trim().toUpperCase();
        return ALLOWED_ACCOUNT_STATUSES.contains(normalized) ? normalized : "ACTIVE";
    }
}