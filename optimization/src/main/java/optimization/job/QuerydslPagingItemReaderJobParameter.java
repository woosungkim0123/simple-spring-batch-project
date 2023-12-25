package optimization.job;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

@Getter
@NoArgsConstructor
public class QuerydslPagingItemReaderJobParameter {
    private LocalDate date;

    /**
     * 기본값을 정하지 않으면 테스트 시 NullPointerException이 발생한다.
     */
    @Value("#{jobParameters['date'] ?: T(java.time.LocalDate).now().toString()}")
    public void setDate(String date) {
        this.date = parse(date, ofPattern("yyyy-MM-dd"));
    }
}
