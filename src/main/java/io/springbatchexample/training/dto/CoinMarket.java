package io.springbatchexample.training.dto;

import lombok.*;


@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoinMarket {
    String market;
    String korean_name;
    String english_name;
}
