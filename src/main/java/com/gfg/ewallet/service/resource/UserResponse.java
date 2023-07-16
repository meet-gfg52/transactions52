package com.gfg.ewallet.service.resource;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponse {

    private String userName;
    private String email;
    private String phoneNumber;
    private String balance;


}
