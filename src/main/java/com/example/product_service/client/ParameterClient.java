
package com.example.product_service.client;

import com.example.product_service.dto.CurrencyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "parameter-service", url = "http://localhost:8083")
public interface ParameterClient {


    @GetMapping("/api/currencies/{code}")
    CurrencyDTO getByCode(@PathVariable("code") String code);
}