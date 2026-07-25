package backend.service;

import backend.entity.IOC;
import backend.entity.User;
import backend.repository.IOCRepository;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class IOCService {

    @Autowired
    private IOCRepository iocRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public List<IOC> getAllIOCs() {
        return iocRepository.findAll();
    }

    public IOC getIOCById(Long id) {
        return iocRepository.findById(id).orElse(null);
    }

    public IOC saveIOC(IOC ioc) {
        IOC saved = iocRepository.save(ioc);
        auditLogService.createLog("CREATE_IOC",
                "New IOC added: " + saved.getValue() + " (Type: " + saved.getType() + ", Risk: " + saved.getRiskLevel() + ")",
                getCurrentUser(), null);
        return saved;
    }

    public IOC updateIOC(Long id, IOC updatedIOC) {
        IOC existingIOC = iocRepository.findById(id).orElse(null);
        if (existingIOC == null) return null;

        existingIOC.setType(updatedIOC.getType());
        existingIOC.setValue(updatedIOC.getValue());
        existingIOC.setRiskLevel(updatedIOC.getRiskLevel());
        existingIOC.setSource(updatedIOC.getSource());
        existingIOC.setStatus(updatedIOC.getStatus());

        IOC saved = iocRepository.save(existingIOC);
        auditLogService.createLog("UPDATE_IOC",
                "IOC updated: " + saved.getValue() + " (Status: " + saved.getStatus() + ")",
                getCurrentUser(), null);
        return saved;
    }

    public void deleteIOC(Long id) {
        IOC ioc = iocRepository.findById(id).orElse(null);
        if (ioc != null) {
            iocRepository.delete(ioc);
            auditLogService.createLog("DELETE_IOC",
                    "IOC deleted: " + ioc.getValue() + " (Type: " + ioc.getType() + ")",
                    getCurrentUser(), null);
        }
    }
}