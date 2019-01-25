package com.github.skystardust.ultracoresponge;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "ultracore-sponge",
        name = "UltraCore Sponge",
        version = "1.0-SNAPSHOT",
        authors = {
                "SkyStardust"
        }
)
public class UltraCoreSponge {

    @Inject
    private Logger logger;

}
