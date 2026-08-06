package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.IOC;
import backend.repository.IOCRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IOCCorrelationService {

    private final IOCRepository iocRepository;

    public IOCCorrelationService(IOCRepository iocRepository) {
        this.iocRepository = iocRepository;
    }

    public Optional<IOC> checkIOC(SecurityEvent event) {

        if (event.getIndicator() == null ||
                event.getIndicator().trim().isEmpty()) {

            return Optional.empty();
        }

        String indicator = event.getIndicator().trim();

        System.out.println("\n========== IOC CORRELATION ==========");
        System.out.println("Indicator      : " + indicator);
        System.out.println("Indicator Type : " + event.getIndicatorType());

        Optional<IOC> result =
                iocRepository.findByValue(indicator);

        if (result.isPresent()) {

            IOC ioc = result.get();

            // Ignore disabled/inactive IOC
            if (ioc.getStatus() != null &&
                    !ioc.getStatus().equalsIgnoreCase("ACTIVE")) {

                System.out.println(
                        "IOC found but status is not ACTIVE."
                );

                return Optional.empty();
            }

            System.out.println("🚨 IOC MATCH FOUND");
            System.out.println("IOC ID     : " + ioc.getId());
            System.out.println("Type       : " + ioc.getType());
            System.out.println("Value      : " + ioc.getValue());
            System.out.println("Risk       : " + ioc.getRiskLevel());
            System.out.println("Source     : " + ioc.getSource());

            return Optional.of(ioc);
        }

        System.out.println("No IOC match found.");

        return Optional.empty();
    }
}