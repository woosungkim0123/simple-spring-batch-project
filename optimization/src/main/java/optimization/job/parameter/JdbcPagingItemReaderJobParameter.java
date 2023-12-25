package optimization.job.parameter;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

@Getter
@NoArgsConstructor
public class JdbcPagingItemReaderJobParameter {
    private LocalDate date;

    @Value("#{jobParameters['date'] ?: T(java.time.LocalDate).now().toString()}")
    public void setDate(String date) {
        this.date = parse(date, ofPattern("yyyy-MM-dd"));
    }
}
