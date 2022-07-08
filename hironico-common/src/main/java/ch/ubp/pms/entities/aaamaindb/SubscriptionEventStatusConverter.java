package ch.ubp.pms.entities.aaamaindb;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class SubscriptionEventStatusConverter implements AttributeConverter<SubscriptionEventStatus, Long> {
    @Override
    public Long convertToDatabaseColumn(SubscriptionEventStatus subscriptionEventStatus) {
        if (subscriptionEventStatus == null) {
            return null;
        }

        return subscriptionEventStatus.getId();
    }

    @Override
    public SubscriptionEventStatus convertToEntityAttribute(Long statusCode) {
        if (statusCode == null) {
            return null;
        }

        return SubscriptionEventStatus.of(statusCode);
    }
}
