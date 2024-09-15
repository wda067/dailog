package com.dailog.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Getter;

/**
 * { "code": "400", "message": "잘못된 요청입니다.", "validation": { "title": "값을 입력해 주세요." } }
 */
@Getter
//@JsonInclude(value = Include.NON_EMPTY)  //빈 값은 제외
public class ErrorResponse {

    private final String code;
    private final String message;
    //데이터를 전달할 때 Map 사용은 지양
    //private final Map<String, String> validation = new HashMap<>();
    private final ObjectNode validation;

    @Builder
    public ErrorResponse(String code, String message, ObjectNode validation) {
        this.code = code;
        this.message = message;
        this.validation = initializeValidation(validation);
    }

    private ObjectNode initializeValidation(ObjectNode validation) {
        if (validation == null) {
            return new ObjectMapper().createObjectNode();
        }
        return validation;
    }

    public void addValidation(String field, String message) {
        this.validation.put(field, message);
    }
}
