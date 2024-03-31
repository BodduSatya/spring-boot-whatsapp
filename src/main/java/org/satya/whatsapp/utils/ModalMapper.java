package org.satya.whatsapp.utils;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

@Controller
public class ModalMapper {

    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }
}
