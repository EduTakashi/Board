package dio.dto;

import dio.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO(Long id, String name, BoardColumnKindEnum kind, int cardsAmount) {

}
