package io.github.nucleuspowered.nucleus.modules.powertool;

import io.github.nucleuspowered.nucleus.internal.TypeTokens;
import io.github.nucleuspowered.nucleus.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.storage.dataobjects.keyed.DataKey;

public class PowertoolKeys {

    public final static DataKey.MapListKey<String, String, IUserDataObject> POWERTOOLS
            = DataKey.ofMapList(
                    TypeTokens.STRING,
                    TypeTokens.STRING,
                    IUserDataObject.class,
                    "powertools"
            );
}
