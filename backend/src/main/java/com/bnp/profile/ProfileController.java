package com.bnp.profile;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUser currentUser;

    // ---------- Addresses ----------
    @GetMapping("/addresses")
    public ApiResponse<List<Address>> addresses() {
        return ApiResponse.ok(profileService.addresses(currentUser.id()));
    }

    @PostMapping("/addresses")
    public ApiResponse<Address> addAddress(@RequestBody Address address) {
        return ApiResponse.ok("Address added", profileService.addAddress(currentUser.id(), address));
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<Address> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        return ApiResponse.ok("Address updated", profileService.updateAddress(currentUser.id(), id, address));
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        profileService.deleteAddress(currentUser.id(), id);
        return ApiResponse.ok("Address deleted", null);
    }

    // ---------- Company profile ----------
    @GetMapping("/company")
    public ApiResponse<CompanyProfile> getCompany() {
        return ApiResponse.ok(profileService.companyProfile(currentUser.id()));
    }

    @PutMapping("/company")
    public ApiResponse<CompanyProfile> saveCompany(@RequestBody CompanyProfile company) {
        return ApiResponse.ok("Company profile saved", profileService.saveCompanyProfile(currentUser.id(), company));
    }

    // ---------- Authenticity documents ----------
    @GetMapping("/documents")
    public ApiResponse<List<AuthenticityDocument>> documents() {
        return ApiResponse.ok(profileService.documents(currentUser.id()));
    }

    @PostMapping("/documents")
    public ApiResponse<AuthenticityDocument> addDocument(@RequestBody AuthenticityDocument doc) {
        return ApiResponse.ok("Document uploaded", profileService.addDocument(currentUser.id(), doc));
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        profileService.deleteDocument(currentUser.id(), id);
        return ApiResponse.ok("Document deleted", null);
    }
}
