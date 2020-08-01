package com.ownimage.perception.pixelMap.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public PixelChainService pixelChainService() {
        return new PixelChainService();
    }

    @Bean
    public PixelMapActionService pixelMapActionService() {
        return new PixelMapActionService();
    }

    @Bean
    public PixelMapApproximationService pixelMapApproximationService() {
        return new PixelMapApproximationService();
    }

    @Bean
    public PixelMapChainGenerationService pixelMapChainGenerationService() {
        return new PixelMapChainGenerationService();
    }

    @Bean
    public PixelMapService pixelMapService() {
        return new PixelMapService();
    }

    @Bean
    public PixelMapTransformService pixelMapTransformService() {
        return new PixelMapTransformService();
    }

    @Bean
    public PixelService pixelService() {
        return new PixelService();
    }

    @Bean
    public VertexService vertexService() {
        return new VertexService();
    }

    @Bean
    public PixelMapValidationService pixelMapValidationService() {
        return new PixelMapValidationService();
    }

}
