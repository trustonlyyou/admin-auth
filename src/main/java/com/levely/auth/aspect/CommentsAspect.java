package com.levely.auth.aspect;

import com.levely.auth.annotaion.Comments;
import com.levely.auth.dto.WalletPointIssueHist;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Aspect
@Component
public class CommentsAspect {

    /**
     * 어노테이션이 붙은 메서드를 감사서 실행 결과와ㅗ 경과 시간을 기록한다.
     * 성능 보호를 위해 INFO / ERROR 로그가 모두 비활성화된 경우네는
     * 부가계산 없이 실제 머세드만 바로 호출한다.
     */
    @Around("@annotation(comments)")
    public Object log(ProceedingJoinPoint joinPoint, Comments comments) throws Throwable {
        boolean infoEnable = log.isInfoEnabled();
        boolean errorEnable = log.isErrorEnabled();

        if (!infoEnable && !errorEnable) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String runtimeMethod = signature.getMethod().getName();
        String methodName = StringUtils.hasText(comments.method()) ? comments.method() : runtimeMethod;
        String desc = StringUtils.hasText(comments.desc()) ? comments.desc() : methodName;

        boolean hasParamKeys = !ObjectUtils.isEmpty(comments.params());
        long startedAtNanos = System.nanoTime();

        try {
            Object result = joinPoint.proceed();

            if (infoEnable) {
                String elapsedSeconds = formatElapsedSeconds(System.nanoTime() - startedAtNanos);
                String resultSummary = summarizeResult(result);

                if (hasParamKeys) {
                    String params = formatParams(signature, comments.params(), joinPoint.getArgs());
                    log.info(">> {}[{}][{}][{}]", desc, resultSummary, elapsedSeconds, params);
                } else {
                    log.info(">> {}[{}][{}]", desc, resultSummary, elapsedSeconds);
                }
            }

            return result;
        } catch (Throwable throwable) {

            if (errorEnable) {
                String elapsedSecond = formatElapsedSeconds(System.nanoTime() - startedAtNanos);

                if (hasParamKeys) {
                    String params = formatParams(signature, comments.params(), joinPoint.getArgs());
                    log.error(">>> {}[E][{}][{}][{}] errorType={}, message={}",
                            desc, elapsedSecond, methodName, params,
                            throwable.getClass().getName(),
                            safeMessage(throwable), throwable
                    );
                } else {
                    log.error(">> {}[E]{{}][{}] errorType={}, message={}",
                            desc, elapsedSecond, methodName, throwable.getClass().getName(),
                            safeMessage(throwable), throwable
                    );
                }
            }
            throw throwable;
        }
    }

    private Object safeMessage(Throwable throwable) {
        String message = Objects.toString(throwable.getMessage(), "");
        return message.replace("\r", " ").replace("\n", " ");
    }

    private String formatParams(MethodSignature signature, String[] paramNames, Object[] args) {
        if (ObjectUtils.isEmpty(paramNames)) {
            return "{}";
        }

        if (!ObjectUtils.isEmpty(args) && paramNames.length == 1 && args.length == 1) {
            if (args[0] instanceof Persistable<?>) {
                return summarizeArg(args[0]);
            }
            return '{' + paramNames[0] + "=" + summarizeArg(args[0]) + '}';
        }

        String[] runtimeParamNames = signature.getParameterNames();
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;

        for (int i = 0; i < paramNames.length; i++) {
            String key = paramNames[i];
            String value = findArgValue(key, i, runtimeParamNames, args);

            if (value == null) {
                value = "<missing>";
            }
            first = appendEntry(builder, first, key, value);
        }

        builder.append('}');
        return builder.toString();
    }

    private boolean appendEntry(StringBuilder builder, boolean first, String key, String value) {
        if (!first) {
            builder.append(", ");
        }
        builder.append(key).append('=').append(value);

        return false;
    }

    private String findArgValue(String key, int index, String[] runtimeParamNames, Object[] args) {
        if (!StringUtils.hasText(key) || ObjectUtils.isEmpty(args)) {
            return null;
        }

        if (!ObjectUtils.isEmpty(runtimeParamNames)) {
            int searchLength = Math.min(runtimeParamNames.length, args.length);

            for (int i = 0; i < searchLength; i++) {
                if (key.equals(runtimeParamNames[i])) {
                    return summarizeArg(args[i]);
                }
            }
        }

        if (index < args.length) {
            return summarizeArg(args[index]);
        }

        return null;
    }

    private String summarizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }

        Class<?> type = arg.getClass();

        if (arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean || type.isEnum()) {
            return String.valueOf(arg);
        }

        if (ObjectUtils.isArray(arg)) {
            return ObjectUtils.nullSafeToString(arg);
        }

        if (arg instanceof Persistable<?> persistable) {
            return summarizePersistable(type, persistable);
        }

        return ClassUtils.getShortName(type) + "@" + Integer.toHexString(System.identityHashCode(arg));
    }

    private String summarizePersistable(Class<?> type, Persistable<?> persistable) {
        if (persistable instanceof WalletPointIssueHist issueHist) {
            return "{seq=" + issueHist.getSeq() + ", tradeType=" + issueHist.getTradeType() + '}';
        }

        Object id = persistable.getId();

        if (isWalletPointSesqEntity(type, id)) {
            return "{seq=" + id + '}';
        }
        return "{id=" + id + '}';
    }

    private boolean isWalletPointSesqEntity(Class<?> type, Object id) {
        return id instanceof String
                && type.getPackageName().startsWith("kr.co.kcp.npgdev.piggy.domain.point.persistence.entity")
                && !"WalletPointTranId".equals(type.getSimpleName());
    }


    private String summarizeResult(Object result) {
        if (result instanceof Boolean boolResult) {
            return boolResult ? "T" : "F";
        }

        Integer count = extractCount(result);

        if (count != null) {
            return String.valueOf(count);
        }

        return result == null ? "0" : "1";
    }

    private Integer extractCount(Object result) {
        if (result == null) {
            return 0;
        }

        if (result instanceof Collection<?> collection) {
            return collection.size();
        }

        if (result instanceof Map<?,?> map) {
            return map.size();
        }

        if (result instanceof Optional<?> optional) {
            return optional.isPresent() ? 1 : 0;
        }

        if (ObjectUtils.isArray(result)) {
            return Array.getLength(result);
        }

        return null;
    }

    private String formatElapsedSeconds(long totalTimeNanos) {
        long roundedMillis = (totalTimeNanos + 500_000L) / 1_000_000L;
        long seconds = roundedMillis / 1_000L;
        long millis = roundedMillis % 1_000L;

        StringBuilder builder = new StringBuilder(16);
        builder.append(seconds).append('.');
        appendThreeDigits(builder, millis);

        return builder.toString();
    }

    private void appendThreeDigits(StringBuilder builder, long value) {
        if (value < 100) {
            builder.append('0');
        }

        if (value < 10) {
            builder.append('0');
        }

        builder.append(value);
    }
}























