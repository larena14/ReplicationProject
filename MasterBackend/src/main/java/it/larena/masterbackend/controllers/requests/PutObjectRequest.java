package it.larena.masterbackend.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PutObjectRequest {

    @NotNull
    @NotBlank
    private String instanceId;

    @NotNull
    @NotBlank
    private String key;

    @NotNull
    @NotEmpty
    private byte[] fileBytes;

    public PutObjectRequest(@NotNull @NotBlank String instanceId, @NotNull @NotBlank String key, @NotNull @NotBlank byte[] fileBytes) {
        this.instanceId = instanceId;
        this.key = key;
        this.fileBytes = fileBytes;
    }
}
