package club.p6e.coat.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Banner Service Impl
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = BannerService.class,
        ignored = BannerServiceImpl.class
)
public class BannerServiceImpl implements BannerService {

    @Override
    public void execute() {
        // http://patorjk.com/software/taag/#p=display&f=Ogre&t=P6e%20Coat%20File
        System.out.println("""
                   ___  __            ___            _       ___ _ _     \s
                  / _ \\/ /_   ___    / __\\___   __ _| |_    / __(_) | ___\s
                 / /_)/ '_ \\ / _ \\  / /  / _ \\ / _` | __|  / _\\ | | |/ _ \\
                / ___/| (_) |  __/ / /__| (_) | (_| | |_  / /   | | |  __/
                \\/     \\___/ \\___| \\____/\\___/ \\__,_|\\__| \\/    |_|_|\\___|
                                                                         \s""");
    }

}
