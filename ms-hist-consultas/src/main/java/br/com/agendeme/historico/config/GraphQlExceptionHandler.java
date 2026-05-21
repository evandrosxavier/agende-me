package br.com.agendeme.historico.config;

import br.com.agendeme.historico.excecoes.BusinessException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof BusinessException be) {
            return GraphqlErrorBuilder.newError(env)
                    .message(be.getMessage())
                    .errorType(mapErrorType(be.getHttpStatus()))
                    .build();
        }
        return null;
    }

    private ErrorType mapErrorType(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND)   return ErrorType.NOT_FOUND;
        if (status == HttpStatus.BAD_REQUEST) return ErrorType.BAD_REQUEST;
        if (status == HttpStatus.FORBIDDEN)   return ErrorType.FORBIDDEN;
        if (status == HttpStatus.UNAUTHORIZED) return ErrorType.UNAUTHORIZED;
        return ErrorType.INTERNAL_ERROR;
    }
}

