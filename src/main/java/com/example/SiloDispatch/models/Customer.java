package com.example.SiloDispatch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("customers")
public class Customer {
    @Id
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String pincode;
}
