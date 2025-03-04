package whocraft.tardis_refined.common.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import whocraft.tardis_refined.TardisRefined;
import whocraft.tardis_refined.common.hum.HumEntry;
import whocraft.tardis_refined.common.hum.TardisHums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HumProvider implements DataProvider {

    protected final DataGenerator generator;
    private final boolean addDefaults;
    protected Map<ResourceLocation, HumEntry> data = new HashMap<>();

    public HumProvider(DataGenerator generator) {
        this(generator, true);
    }

    public HumProvider(DataGenerator generator, boolean addDefaults) {
        Preconditions.checkNotNull(generator);
        this.generator = generator;
        this.addDefaults = addDefaults;
    }

    protected void addHums() {
    }

    @Override
    public CompletableFuture<?> run(CachedOutput arg) {
        this.data.clear();

        final List<CompletableFuture<?>> futures = new ArrayList<>();

        if (this.addDefaults) {
            TardisHums.registerDefaultHums();
            data.putAll(TardisHums.getDefaultHums());
            addHum(TardisHums.getDefaultHum());
            addHum(TardisHums.VICTORIAN);
            addHum(TardisHums.TOYOTA);
            addHum(TardisHums.CLASSIC);
            addHum(TardisHums.CAVE);
            addHum(TardisHums.CRIMSON_FOREST);
            addHum(TardisHums.BASALT_DELTAS);
            addHum(TardisHums.NETHER_WASTES);
            addHum(TardisHums.SOUL_SAND_VALLEY);
            addHum(TardisHums.WARPED_FOREST);
            addHum(TardisHums.UNDER_WATER);
        }

        this.addHums();

        if (!data.isEmpty()) {
            data.forEach((key, hum) -> {
                TardisRefined.LOGGER.debug("Writing Hum {}", key);
                try {
                    JsonObject currentHum = HumEntry.codec().encodeStart(JsonOps.INSTANCE, hum).get()
                            .ifRight(right -> {
                                TardisRefined.LOGGER.error(right.message());
                            }).orThrow().getAsJsonObject();
                    String outputPath = "data/" + hum.getIdentifier().getNamespace() + "/" + TardisHums.getReloadListener().getFolderName() + "/" + hum.getIdentifier().getPath().replace("/", "_") + ".json";
                    futures.add(DataProvider.saveStable(arg, currentHum, generator.getPackOutput().getOutputFolder().resolve(outputPath)));
                } catch (Exception exception) {
                    TardisRefined.LOGGER.debug("Issue writing Hum {}! Error: {}", hum.getIdentifier(), exception.getMessage());
                }
            });
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Hums";
    }

    protected void addHum(HumEntry hum) {
        TardisRefined.LOGGER.info("Adding Hum to datagen {}", hum.getIdentifier());
        data.put(hum.getIdentifier(), hum);
    }

}
