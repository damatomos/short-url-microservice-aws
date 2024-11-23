package br.com.damatomos.create_short_url;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UrlData {

    private String originalUrl;
    private long expirationTime;

}
