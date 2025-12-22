package shootingplace;

import com.shootingplace.shootingplace.ShootingplaceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = ShootingplaceApplication.class)
class ShootingplaceApplicationTests {

	@Test
	void contextLoads() {
	}

}
