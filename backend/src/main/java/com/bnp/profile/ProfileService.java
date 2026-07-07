package com.bnp.profile;

import com.bnp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AddressRepository addressRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final AuthenticityDocumentRepository documentRepository;

    // ---------- Addresses ----------
    public List<Address> addresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Transactional
    public Address addAddress(Long userId, Address address) {
        address.setId(null);
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getPrimaryAddress())) {
            addressRepository.findByUserId(userId).forEach(a -> {
                a.setPrimaryAddress(false);
                addressRepository.save(a);
            });
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, Address req) {
        Address a = addressRepository.findById(addressId)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        a.setLabel(req.getLabel());
        a.setLine1(req.getLine1());
        a.setLine2(req.getLine2());
        a.setCity(req.getCity());
        a.setState(req.getState());
        a.setCountry(req.getCountry());
        a.setPincode(req.getPincode());
        if (req.getPrimaryAddress() != null) a.setPrimaryAddress(req.getPrimaryAddress());
        return addressRepository.save(a);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        addressRepository.delete(a);
    }

    // ---------- Company profile ----------
    public CompanyProfile companyProfile(Long userId) {
        return companyProfileRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public CompanyProfile saveCompanyProfile(Long userId, CompanyProfile req) {
        CompanyProfile cp = companyProfileRepository.findByUserId(userId).orElseGet(CompanyProfile::new);
        cp.setUserId(userId);
        cp.setCompanyName(req.getCompanyName());
        cp.setDesignation(req.getDesignation());
        cp.setIndustry(req.getIndustry());
        cp.setWebsite(req.getWebsite());
        cp.setAbout(req.getAbout());
        cp.setLogoUrl(req.getLogoUrl());
        cp.setFoundedYear(req.getFoundedYear());
        cp.setTeamSize(req.getTeamSize());
        return companyProfileRepository.save(cp);
    }

    // ---------- Authenticity documents ----------
    public List<AuthenticityDocument> documents(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Transactional
    public AuthenticityDocument addDocument(Long userId, AuthenticityDocument doc) {
        doc.setId(null);
        doc.setUserId(userId);
        return documentRepository.save(doc);
    }

    @Transactional
    public void deleteDocument(Long userId, Long docId) {
        AuthenticityDocument d = documentRepository.findById(docId)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Document", docId));
        documentRepository.delete(d);
    }
}
