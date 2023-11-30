package io.springbatchexample.training.custom;

import io.springbatchexample.training.dto.OneLineDto;
import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomPassThroughLineAggregator<T> implements LineAggregator<T> {
    @Override
    public String aggregate(T item) {

        if(item instanceof OneLineDto) {
            return item.toString() + "_item";
        }
        return item.toString();
    }
}
