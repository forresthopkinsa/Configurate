/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.jackson;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationVisitor;

import java.io.IOException;

class JacksonVisitor implements ConfigurationVisitor<JsonGenerator, Void, IOException> {

    static final ThreadLocal<JacksonVisitor> INSTANCE = ThreadLocal.withInitial(JacksonVisitor::new);

    private @Nullable ConfigurationNode start;

    @Override
    public JsonGenerator newState() {
        throw new UnsupportedOperationException("Generator must be provided");
    }

    @Override
    public void beginVisit(final ConfigurationNode node, final JsonGenerator state) {
        this.start = node;
    }

    @Override
    public void enterNode(final ConfigurationNode node, final JsonGenerator generator) throws IOException {
        //generateComment(generator, ent.getValue(), false);
        final @Nullable ConfigurationNode parent = node.parent();
        if (node != this.start && parent != null && parent.isMap()) {
            generator.writeFieldName(requireNonNull(node.key(), "Node must have key to be a value in a mapping").toString());
        }
    }

    /*private void generateComment(JsonGenerator generator, ConfigurationNode node, boolean inArray) throws IOException {
        if (node instanceof CommentedConfigurationNode) {
            final Optional<String> comment = ((CommentedConfigurationNode) node).getComment();
            if (comment.isPresent()) {
                if (indent == 0) {
                    generator.writeRaw("/*");
                    generator.writeRaw(comment.get().replaceAll("\\* /", ""));
                    generator.writeRaw("* /");
                } else {
                    for (Iterator<String> it = LINE_SPLITTER.split(comment.get()).iterator(); it.hasNext();) {
                        generator.writeRaw("// ");
                        generator.writeRaw(it.next());
                        generator.getPrettyPrinter().beforeObjectEntries(generator);
                        if (it.hasNext()) {
                            generator.writeRaw(SYSTEM_LINE_SEPARATOR);
                        }
                    }
                    if (inArray) {
                        generator.writeRaw(SYSTEM_LINE_SEPARATOR);
                    }
                }
            }
        }
    }*/

    @Override
    public void enterMappingNode(final ConfigurationNode node, final JsonGenerator state) throws IOException {
        state.writeStartObject();
    }

    @Override
    public void enterListNode(final ConfigurationNode node, final JsonGenerator state) throws IOException {
        state.writeStartArray();
    }

    @Override
    public void enterScalarNode(final ConfigurationNode node, final JsonGenerator generator) throws IOException {
        final @Nullable Object value = node.get();
        if (value instanceof Double) {
            generator.writeNumber((Double) value);
        } else if (value instanceof Float) {
            generator.writeNumber((Float) value);
        } else if (value instanceof Long) {
            generator.writeNumber((Long) value);
        } else if (value instanceof Integer) {
            generator.writeNumber((Integer) value);
        } else if (value instanceof Boolean) {
            generator.writeBoolean((Boolean) value);
        } else if (value instanceof byte[]) {
            generator.writeBinary((byte[]) value);
        } else if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value.toString());
        }
    }

    @Override
    public void exitMappingNode(final ConfigurationNode node, final JsonGenerator state) throws IOException {
        state.writeEndObject();
    }

    @Override
    public void exitListNode(final ConfigurationNode node, final JsonGenerator state) throws IOException {
        state.writeEndArray();
    }

    @Override
    public Void endVisit(final JsonGenerator state) throws IOException {
        state.flush();
        this.start = null;
        return null;
    }

}
