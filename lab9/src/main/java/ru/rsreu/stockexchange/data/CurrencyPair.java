package ru.rsreu.stockexchange.data;

import lombok.Getter;
import lombok.Setter;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;

@Getter
@Setter
public class CurrencyPair {
    private final CurrencyTypeEnum baseCurrency;
    private final CurrencyTypeEnum quoteCurrency;

    public CurrencyPair(CurrencyTypeEnum baseCurrency, CurrencyTypeEnum quoteCurrency) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
    }

    @Override
    public String toString() {
        return baseCurrency.toString() + "->" + quoteCurrency.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CurrencyPair that = (CurrencyPair) obj;
        return baseCurrency == that.baseCurrency && quoteCurrency == that.quoteCurrency;
    }

    @Override
    public int hashCode() {
        int result = baseCurrency != null ? baseCurrency.hashCode() : 0;
        result = 31 * result + (quoteCurrency != null ? quoteCurrency.hashCode() : 0);
        return result;
    }
}
