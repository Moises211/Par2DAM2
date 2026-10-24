package com.example.reusisam.DTOS;

public class ProductsDTO {
    private Integer code;
    private String name;
    private boolean status;

    // Constructor vacío requerido por Gson
    public ProductsDTO() {
    }

    public ProductsDTO(Integer code, String name, boolean status) {
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}