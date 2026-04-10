package cc.spea.gamingtimelimit.forge;

import net.minecraftforge.fml.loading.ImmediateWindowProvider;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class ForgeNoVizWindowProvider implements ImmediateWindowProvider {
    private static final String FORGE_MODULE = "net.minecraftforge.forge";
    private static final String NO_VIZ_FALLBACK = "net.minecraftforge.client.loading.NoVizFallback";

    private volatile Class<?> noVizFallbackClass;

    @Override
    public String name() {
        return "gamingtimelimit_noviz";
    }

    @Override
    public Runnable initialize(String[] arguments) {
        return () -> {};
    }

    @Override
    public void updateFramebufferSize(IntConsumer width, IntConsumer height) {
    }

#if MC_VER > MC_1_21_11
    @Override
    public long setupMinecraftWindow(int width, int height, String title, long monitor, Supplier<Object> monitorCreator) {
        return this.invoke("windowHandoff", new Class<?>[] { int.class, int.class, String.class, long.class, Supplier.class }, width, height, title, monitor, monitorCreator);
    }
#else
    @Override
    public long setupMinecraftWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        return this.invoke("windowHandoff", new Class<?>[] { IntSupplier.class, IntSupplier.class, Supplier.class, LongSupplier.class }, width, height, title, monitor);
    }
#endif

    @Override
    public boolean positionWindow(Optional<Object> monitor, IntConsumer width, IntConsumer height, IntConsumer x, IntConsumer y) {
        Boolean positioned = this.invoke("windowPositioning", new Class<?>[] { Optional.class, IntConsumer.class, IntConsumer.class, IntConsumer.class, IntConsumer.class }, monitor, width, height, x, y);
        return Boolean.TRUE.equals(positioned);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> loadingOverlay(Supplier<?> minecraft, Supplier<?> reload, Consumer<Optional<Throwable>> errorCallback, boolean fadeIn) {
        return (Supplier<T>) this.invoke("loadingOverlay", new Class<?>[] { Supplier.class, Supplier.class, Consumer.class, boolean.class }, minecraft, reload, errorCallback, fadeIn);
    }

    @Override
    public void updateModuleReads(ModuleLayer layer) {
        this.noVizFallbackClass = this.resolveNoVizFallback(layer);
    }

    @Override
    public void periodicTick() {
    }

    @Override
    public String getGLVersion() {
        return this.invoke("glVersion", new Class<?>[0]);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) {
        Class<?> fallbackClass = this.noVizFallbackClass;
        if (fallbackClass == null) {
            fallbackClass = this.resolveNoVizFallback(ModuleLayer.boot());
            this.noVizFallbackClass = fallbackClass;
        }

        try {
            Method method = fallbackClass.getMethod(methodName, parameterTypes);
            Object result = method.invoke(null, args);
            if (result instanceof LongSupplier longSupplier) {
                return (T) Long.valueOf(longSupplier.getAsLong());
            }
            return (T) result;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Forge no-visual fallback method " + methodName, exception);
        }
    }

    private Class<?> resolveNoVizFallback(ModuleLayer layer) {
        ClassLoader loader = layer.findLoader(FORGE_MODULE);
        if (loader == null) {
            throw new IllegalStateException("Could not resolve Forge module classloader for " + FORGE_MODULE);
        }

        try {
            return Class.forName(NO_VIZ_FALLBACK, true, loader);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Could not load " + NO_VIZ_FALLBACK + " from the Forge module classloader.", exception);
        }
    }
}
