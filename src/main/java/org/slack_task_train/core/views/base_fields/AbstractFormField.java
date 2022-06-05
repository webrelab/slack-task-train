package org.slack_task_train.core.views.base_fields;

import com.slack.api.model.block.element.BlockElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slack_task_train.core.SlackTaskTrainException;
import org.slack_task_train.core.ifaces.IFormField;
import org.slack_task_train.core.views.Element;

import java.util.*;
import java.util.function.Supplier;


public abstract class AbstractFormField extends Element implements IFormField {
    private String value = "";
    private String id = UUID.randomUUID().toString();
    private final Map<Object, Object> cache = new HashMap<>();

    protected final void setId(final String id) {
        this.id = id;
    }

    protected BlockElement getCachedElement(final Supplier<BlockElement> elementGenerator, final Object... params) {
        final Optional<BlockElement> elementOptional = getFromCache(params);
        if (elementOptional.isPresent()) {
            return elementOptional.get();
        } else {
            final BlockElement element = elementGenerator.get();
            setToCache(element, params);
            return element;
        }
    }

    private Optional<BlockElement> getFromCache(final Object... params) {
        final BlockElement element = getFromCache(cache, params);
        return Objects.isNull(element) ? Optional.empty() : Optional.of(element);
    }

    private void setToCache(final BlockElement element, final Object... params) {
        setToCache(cache, element, params);
    }

    @SuppressWarnings("unchecked")
    private void setToCache(final Object cache, final BlockElement element, final Object... params) {
        if (!(cache instanceof Map)) {
            return;
        }
        final Map<Object, Object> current = (Map<Object, Object>) cache;
        if (!(current.containsKey(params[0]))) {
            if (params.length > 1) {
                final Map<Object, Object> map = new HashMap<>();
                current.put(params[0], map);
            } else {
                current.put(params[0], element);
                return;
            }
        }
        setToCache(current.get(params[0]), element, Arrays.copyOfRange(params, 1, params.length));
    }

    @Nullable
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    private BlockElement getFromCache(final Object cache, final Object... params) {
        if (Objects.isNull(cache)) {
            return null;
        }
        if (cache instanceof Map) {
            final Map<Object, Object> current = (Map<Object, Object>) cache;
            if (params.length == 0 || current.isEmpty()) {
                return null;
            }

            return getFromCache(
                    current.get(params[0]),
                    params.length == 1 ? null : Arrays.copyOfRange(params, 1, params.length));
        }
        if (cache instanceof BlockElement) {
            return (BlockElement) cache;
        }
        return null;
    }

    @Override
    public boolean isFilled() {
        return !value.isEmpty();
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void clearState() {
        value = "";
    }

    @Override
    public boolean getBoolean() {
        throw new SlackTaskTrainException("Поле не поддерживает получение булева значения");
    }

    @Override
    public List<String> getValues() {
        throw new SlackTaskTrainException("Поле не поддерживает получение списка значений");
    }
}
