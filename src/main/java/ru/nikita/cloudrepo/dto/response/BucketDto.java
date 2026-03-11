package ru.nikita.cloudrepo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class BucketDto {
    private String bucketName;
    private ZonedDateTime creationDate;
    private String bucketRegion;
}
