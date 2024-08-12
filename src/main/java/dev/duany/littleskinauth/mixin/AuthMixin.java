package dev.duany.littleskinauth.mixin;

import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.yggdrasil.*;
import com.mojang.datafixers.DataFixer;
import dev.duany.littleskinauth.CustomSessionService;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.util.UserCache;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.Proxy;
import java.net.URL;

@Mixin(MinecraftServer.class)
public class AuthMixin {

    @Mutable
    @Shadow @Final protected ApiServices apiServices;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onSetupServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.apiServices = generateApiServices();
    }
    private ApiServices generateApiServices() {
        final Environment env = new Environment("https://sessionserver.mojang.com",
                "https://api.minecraftservices.com",
                "PROD");

        final MinecraftClient client = MinecraftClient.unauthenticated(Proxy.NO_PROXY);
        URL publicKeySetUrl = HttpAuthenticationService.constantURL( env.servicesHost()+ "/publickeys");
        ServicesKeySet mojangKeySet = YggdrasilServicesKeyInfo.get(publicKeySetUrl, client);
        GameProfileRepository gameProfileRepository = new YggdrasilGameProfileRepository(Proxy.NO_PROXY, env);;
        UserCache userCache = new UserCache(gameProfileRepository, new File("", "usercache.json"));
        return new ApiServices(new CustomSessionService(Proxy.NO_PROXY, env), mojangKeySet, gameProfileRepository, userCache);
    }
}
