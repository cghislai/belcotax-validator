package com.charlyghislain.belcotax;

import java.util.List;
import java.util.Map;

public class BelcotaxTagContextMappings {

    Map<String, List<String>> FRENCH_MAPPINGS = Map.of(
            "Année des dépenses", List.of("0002_inkomstenjaar"),
            "Numéro d’ordre de l’attestation", List.of("v0002_inkomstenjaar")
    );
}
