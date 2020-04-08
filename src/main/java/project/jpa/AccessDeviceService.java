package project.jpa;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class AccessDeviceService {

    @Resource
    private AccessDeviceRepository accessDeviceRepository;

    public Optional<List<AccessDevice>> findByCustomKey(String customerKey) {
        return accessDeviceRepository.findByCustomKey(customerKey);
    }
}
