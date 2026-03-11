package com.binbang.backend.address.controller;

import com.binbang.backend.address.dto.AddressResponse;
import com.binbang.backend.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/search")
    public ResponseEntity<List<AddressResponse>> searchAddress(@RequestParam String query){
        List<AddressResponse> addressResponses = addressService.searchAddress(query);

        return ResponseEntity.ok(addressResponses);
    }
}
