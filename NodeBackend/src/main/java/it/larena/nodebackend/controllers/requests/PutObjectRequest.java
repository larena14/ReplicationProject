package it.larena.nodebackend.controllers.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PutObjectRequest {

    @NotNull
    @NotBlank
    private String key;

    @NotNull
    @NotEmpty
    private byte[] fileBytes;

    @JsonCreator
    public PutObjectRequest(@NotNull @NotBlank String key, @NotNull @NotEmpty byte[] fileBytes) {
        this.key = key;
        this.fileBytes = fileBytes;
    }
}
