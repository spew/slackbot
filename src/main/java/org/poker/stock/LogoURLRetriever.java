package org.poker.stock;

import java.util.Optional;

public interface LogoURLRetriever {
    Optional<String> retrieve(String companyName);
}
