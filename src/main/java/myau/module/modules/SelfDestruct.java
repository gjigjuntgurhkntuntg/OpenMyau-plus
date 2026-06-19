package myau.module.modules;

import myau.module.Module;

/**
 * SelfDestruct module has been removed/disabled.
 * Keeping a minimal stub to avoid compile errors in consumers until full deletion is performed.
 */
@Deprecated
public class SelfDestruct extends Module {
    public SelfDestruct() {
        super("Self Destruct", false, false, "Disabled");
        // intentionally no-op
    }
}
