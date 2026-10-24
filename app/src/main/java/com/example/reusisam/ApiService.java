/*package com.example.reusisam;

import com.example.reusisam.DTOS.LoginDTO;
import com.example.reusisam.DTOS.ProductsDTO;
import com.example.reusisam.DTOS.TokenDTO;
import com.example.reusisam.DTOS.UserDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    // Endpoint para iniciar sesión y retornar el token JWT del backend[cite: 2]
    @POST("auth/login")
    Call<TokenDTO> login(@Body LoginDTO loginDTO);

    @GET("products")
    Call<List<ProductsDTO>> getProducts(@Header("Authorization") String token);

    @POST("products")
    Call<Boolean> saveProduct(@Header("Authorization") String token, @Body ProductsDTO product);

    @PUT("products")
    Call<Boolean> updateProduct(@Header("Authorization") String token, @Body ProductsDTO product);

    @HTTP(method = "DELETE", path = "products", hasBody = true)
    Call<Boolean> deleteProduct(@Header("Authorization") String token, @Body Map<String, Integer> body);

    @POST("user/register")
    Call<Boolean> saveUser(@Body UserDTO userDTO);
}*/