package org.satya.whatsapp.config;

import it.auties.whatsapp.api.ClientType;
import it.auties.whatsapp.controller.*;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.chat.ChatBuilder;
import it.auties.whatsapp.model.chat.ChatSpec;
import it.auties.whatsapp.model.info.ContextInfo;
import it.auties.whatsapp.model.info.NewsletterMessageInfo;
import it.auties.whatsapp.model.jid.Jid;
import it.auties.whatsapp.model.jid.JidProvider;
import it.auties.whatsapp.model.message.model.ContextualMessage;
import it.auties.whatsapp.model.mobile.PhoneNumber;
import it.auties.whatsapp.model.sync.HistorySyncMessage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import it.auties.whatsapp.model.newsletter.Newsletter;
import it.auties.whatsapp.model.newsletter.NewsletterMetadata;
import it.auties.whatsapp.model.newsletter.NewsletterSpec;
import it.auties.whatsapp.model.newsletter.NewsletterState;
import it.auties.whatsapp.model.newsletter.NewsletterViewerMetadata;

import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;

class ProtobufControllerSerializer implements ControllerSerializer {
    private static final Path DEFAULT_SERIALIZER_PATH = Path.of(System.getProperty("user.home") + "/.cobalt/");
    private static final String CHAT_PREFIX = "chat_";
    private static final String NEWSLETTER_PREFIX = "newsletter_";
    private static final String STORE_NAME = "store.proto";
    private static final String KEYS_NAME = "keys.proto";
    private static final Map<Path, ProtobufControllerSerializer> serializers = new ConcurrentHashMap();
    private final Path baseDirectory;
    private final ConcurrentMap<UUID, CompletableFuture<Void>> attributeStoreSerializers;
    private LinkedList<UUID> cachedUuids;
    private LinkedList<PhoneNumber> cachedPhoneNumbers;

    public static ControllerSerializer ofDefaultPath() {
        return (ControllerSerializer)Objects.requireNonNull((ProtobufControllerSerializer)serializers.get(DEFAULT_SERIALIZER_PATH));
    }

    public static ControllerSerializer of(Path baseDirectory) {
        ProtobufControllerSerializer known = (ProtobufControllerSerializer)serializers.get(baseDirectory);
        if (known != null) {
            return known;
        } else {
            ProtobufControllerSerializer result = new ProtobufControllerSerializer(baseDirectory);
            serializers.put(baseDirectory, result);
            return result;
        }
    }

    public ProtobufControllerSerializer(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.attributeStoreSerializers = new ConcurrentHashMap();
    }

    public LinkedList<UUID> listIds(ClientType type) {
        if (this.cachedUuids != null) {
            return new ProtobufControllerSerializer.ImmutableLinkedList(this.cachedUuids);
        } else {
            Path directory = this.getHome(type);
            if (Files.notExists(directory, new LinkOption[0])) {
                return ProtobufControllerSerializer.ImmutableLinkedList.empty();
            } else {
                try {
                    Stream<Path> walker = Files.walk(directory, 1, new FileVisitOption[0]).sorted(Comparator.comparing(this::getLastModifiedTime));

                    LinkedList var4;
                    try {
                        var4 = this.cachedUuids = (LinkedList)walker.map(this::parsePathAsId).flatMap(Optional::stream).collect(Collectors.toCollection(LinkedList::new));
                    } catch (Throwable var7) {
                        if (walker != null) {
                            try {
                                walker.close();
                            } catch (Throwable var6) {
                                var7.addSuppressed(var6);
                            }
                        }

                        throw var7;
                    }

                    if (walker != null) {
                        walker.close();
                    }

                    return var4;
                } catch (IOException var8) {
                    return ProtobufControllerSerializer.ImmutableLinkedList.empty();
                }
            }
        }
    }

    public LinkedList<PhoneNumber> listPhoneNumbers(ClientType type) {
        if (this.cachedPhoneNumbers != null) {
            return new ProtobufControllerSerializer.ImmutableLinkedList(this.cachedPhoneNumbers);
        } else {
            Path directory = this.getHome(type);
            if (Files.notExists(directory, new LinkOption[0])) {
                return ProtobufControllerSerializer.ImmutableLinkedList.empty();
            } else {
                try {
                    Stream<Path> walker = Files.walk(directory, 1, new FileVisitOption[0]).sorted(Comparator.comparing(this::getLastModifiedTime));

                    LinkedList var4;
                    try {
                        var4 = this.cachedPhoneNumbers = (LinkedList)walker.map(this::parsePathAsPhoneNumber).flatMap(Optional::stream).collect(Collectors.toCollection(LinkedList::new));
                    } catch (Throwable var7) {
                        if (walker != null) {
                            try {
                                walker.close();
                            } catch (Throwable var6) {
                                var7.addSuppressed(var6);
                            }
                        }

                        throw var7;
                    }

                    if (walker != null) {
                        walker.close();
                    }

                    return var4;
                } catch (IOException var8) {
                    return ProtobufControllerSerializer.ImmutableLinkedList.empty();
                }
            }
        }
    }

    private FileTime getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException var3) {
            return FileTime.fromMillis(0L);
        }
    }

    private Optional<UUID> parsePathAsId(Path file) {
        try {
            return Optional.of(UUID.fromString(file.getFileName().toString()));
        } catch (IllegalArgumentException var3) {
            return Optional.empty();
        }
    }

    private Optional<PhoneNumber> parsePathAsPhoneNumber(Path file) {
        try {
            long longValue = Long.parseLong(file.getFileName().toString());
            return PhoneNumber.ofNullable(longValue);
        } catch (IllegalArgumentException var4) {
            return Optional.empty();
        }
    }

    public CompletableFuture<Void> serializeKeys(Keys keys, boolean async) {
        if (this.cachedUuids != null && !this.cachedUuids.contains(keys.uuid())) {
            this.cachedUuids.add(keys.uuid());
        }

        Path outputFile = this.getSessionFile(keys.clientType(), keys.uuid().toString(), "keys.proto");
        if (async) {
            return CompletableFuture.runAsync(() -> {
                this.writeFile(KeysSpec.encode(keys), "keys.proto", outputFile);
            }).exceptionallyAsync(this::onError);
        } else {
            this.writeFile(KeysSpec.encode(keys), "keys.proto", outputFile);
            return CompletableFuture.completedFuture((Void) null);
        }
    }

    public CompletableFuture<Void> serializeStore(Store store, boolean async) {
        if (this.cachedUuids != null && !this.cachedUuids.contains(store.uuid())) {
            this.cachedUuids.add(store.uuid());
        }

        PhoneNumber phoneNumber = (PhoneNumber)store.phoneNumber().orElse((PhoneNumber) null);
        if (this.cachedPhoneNumbers != null && !this.cachedPhoneNumbers.contains(phoneNumber)) {
            this.cachedPhoneNumbers.add(phoneNumber);
        }

        CompletableFuture<Void> task = (CompletableFuture)this.attributeStoreSerializers.get(store.uuid());
        if (task != null && !task.isDone()) {
            return task;
        } else {
            CompletableFuture<?>[] chatsFutures = this.serializeChatsAsync(store);
            CompletableFuture<?>[] newslettersFutures = this.serializeNewslettersAsync(store);
            CompletableFuture[] dependableFutures = (CompletableFuture[])Stream.of(chatsFutures, newslettersFutures).flatMap(Arrays::stream).toArray((x$0) -> {
                return new CompletableFuture[x$0];
            });
            CompletableFuture<Void> result = CompletableFuture.allOf(dependableFutures).thenRunAsync(() -> {
                Path storePath = this.getSessionFile(store, "store.proto");
                this.writeFile(StoreSpec.encode(store), "store.proto", storePath);
            });
            if (async) {
                return result;
            } else {
                result.join();
                return CompletableFuture.completedFuture((Void) null);
            }
        }
    }

    private CompletableFuture<?>[] serializeChatsAsync(Store store) {
        return (CompletableFuture[])store.chats().stream().map((chat) -> {
            return this.serializeChatAsync(store, chat);
        }).toArray((x$0) -> {
            return new CompletableFuture[x$0];
        });
    }

    private CompletableFuture<Void> serializeChatAsync(Store store, Chat chat) {
        if (!chat.hasUpdate()) {
            return CompletableFuture.completedFuture((Void) null);
        } else {
            String fileName = "chat_" + chat.jid().user() + ".proto";
            Path outputFile = this.getSessionFile(store, fileName);
            return CompletableFuture.runAsync(() -> {
                this.writeFile(ChatSpec.encode(chat), fileName, outputFile);
            }).exceptionallyAsync(this::onError);
        }
    }

    private Void onError(Throwable error) {
        System.Logger logger = System.getLogger("Serializer");
        logger.log(System.Logger.Level.ERROR, error);
        return null;
    }

    private CompletableFuture<?>[] serializeNewslettersAsync(Store store) {
        return (CompletableFuture[])store.newsletters().stream().map((newsletter) -> {
            return this.serializeNewsletterAsync(store, newsletter);
        }).toArray((x$0) -> {
            return new CompletableFuture[x$0];
        });
    }

    private CompletableFuture<Void> serializeNewsletterAsync(Store store, Newsletter newsletter) {
        String fileName = "newsletter_" + newsletter.jid().user() + ".proto";
        Path outputFile = this.getSessionFile(store, fileName);
        return CompletableFuture.runAsync(() -> {
            this.writeFile(NewsletterSpec.encode(newsletter), fileName, outputFile);
        });
    }

    private void writeFile(byte[] object, String fileName, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            Path tempFile = Files.createTempFile(fileName, ".tmp");
            GZIPOutputStream tempFileOutputStream = new GZIPOutputStream(Files.newOutputStream(tempFile));

            try {
                tempFileOutputStream.write(object);
            } catch (Throwable var9) {
                try {
                    tempFileOutputStream.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }

                throw var9;
            }

            tempFileOutputStream.close();
            Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException var10) {
            IOException exception = var10;
            throw new UncheckedIOException("Cannot write file", exception);
        }
    }

    public Optional<Keys> deserializeKeys(ClientType type, UUID id) {
        return this.deserializeKeysFromId(type, id.toString());
    }

    public Optional<Keys> deserializeKeys(ClientType type, String alias) {
        Path file = this.getSessionDirectory(type, alias);
        if (Files.notExists(file, new LinkOption[0])) {
            return Optional.empty();
        } else {
            try {
                return this.deserializeKeysFromId(type, Files.readString(file));
            } catch (IOException var5) {
                return Optional.empty();
            }
        }
    }

    public Optional<Keys> deserializeKeys(ClientType type, long phoneNumber) {
        Path file = this.getSessionDirectory(type, String.valueOf(phoneNumber));
        if (Files.notExists(file, new LinkOption[0])) {
            return Optional.empty();
        } else {
            try {
                return this.deserializeKeysFromId(type, Files.readString(file));
            } catch (IOException var6) {
                return Optional.empty();
            }
        }
    }

    private Optional<Keys> deserializeKeysFromId(ClientType type, String id) {
        Path path = this.getSessionFile(type, id, "keys.proto");

        try {
            GZIPInputStream input = new GZIPInputStream(Files.newInputStream(path));

            Optional var5;
            try {
                var5 = Optional.of(KeysSpec.decode(input.readAllBytes()));
            } catch (Throwable var8) {
                try {
                    input.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }

                throw var8;
            }

            input.close();
            return var5;
        } catch (IOException var9) {
            return Optional.empty();
        }
    }

    public Optional<Store> deserializeStore(ClientType type, UUID id) {
        return this.deserializeStoreFromId(type, id.toString());
    }

    public Optional<Store> deserializeStore(ClientType type, String alias) {
        Path file = this.getSessionDirectory(type, alias);
        if (Files.notExists(file, new LinkOption[0])) {
            return Optional.empty();
        } else {
            try {
                return this.deserializeStoreFromId(type, Files.readString(file));
            } catch (IOException var5) {
                return Optional.empty();
            }
        }
    }

    public Optional<Store> deserializeStore(ClientType type, long phoneNumber) {
        Path file = this.getSessionDirectory(type, String.valueOf(phoneNumber));
        if (Files.notExists(file, new LinkOption[0])) {
            return Optional.empty();
        } else {
            try {
                return this.deserializeStoreFromId(type, Files.readString(file));
            } catch (IOException var6) {
                return Optional.empty();
            }
        }
    }

    private Optional<Store> deserializeStoreFromId(ClientType type, String id) {
        Path path = this.getSessionFile(type, id, "store.proto");
        if (Files.notExists(path, new LinkOption[0])) {
            return Optional.empty();
        } else {
            try {
                GZIPInputStream input = new GZIPInputStream(Files.newInputStream(path));

                Optional var5;
                try {
                    var5 = Optional.of(StoreSpec.decode(input.readAllBytes()));
                } catch (Throwable var8) {
                    try {
                        input.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }

                    throw var8;
                }

                input.close();
                return var5;
            } catch (IOException var9) {
                return Optional.empty();
            }
        }
    }

    public CompletableFuture<Void> attributeStore(Store store) {
        CompletableFuture<Void> oldTask = (CompletableFuture)this.attributeStoreSerializers.get(store.uuid());
        if (oldTask != null) {
            return oldTask;
        } else {
            Path directory = this.getSessionDirectory(store.clientType(), store.uuid().toString());
            if (Files.notExists(directory, new LinkOption[0])) {
                return CompletableFuture.completedFuture((Void) null);
            } else {
                try {
                    Stream<Path> walker = Files.walk(directory);

                    CompletableFuture var7;
                    try {
                        CompletableFuture[] futures = (CompletableFuture[])walker.map((entry) -> {
                            return this.handleStoreFile(store, entry);
                        }).filter(Objects::nonNull).toArray((x$0) -> {
                            return new CompletableFuture[x$0];
                        });
                        CompletableFuture<Void> result = CompletableFuture.allOf(futures).thenRun(() -> {
                            this.attributeStoreContextualMessages(store);
                        });
                        this.attributeStoreSerializers.put(store.uuid(), result);
                        var7 = result;
                    } catch (Throwable var9) {
                        if (walker != null) {
                            try {
                                walker.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (walker != null) {
                        walker.close();
                    }

                    return var7;
                } catch (IOException var10) {
                    IOException exception = var10;
                    return CompletableFuture.failedFuture(exception);
                }
            }
        }
    }

    private void attributeStoreContextualMessages(Store store) {
        store.chats().stream().flatMap((chat) -> {
            return chat.messages().stream();
        }).forEach((message) -> {
            this.attributeStoreContextualMessage(store, message);
        });
    }

    private void attributeStoreContextualMessage(Store store, HistorySyncMessage message) {
        message.messageInfo().message().contentWithContext().flatMap(ContextualMessage::contextInfo).ifPresent((contextInfo) -> {
            this.attributeStoreContextInfo(store, contextInfo);
        });
    }

    private void attributeStoreContextInfo(Store store, ContextInfo contextInfo) {
        Optional var10000 = contextInfo.quotedMessageChatJid();
        Objects.requireNonNull(store);
        var10000 = var10000.flatMap(jid -> store.findChatByJid((JidProvider) jid));
        Objects.requireNonNull(contextInfo);
        var10000.ifPresent(quotedMessageChat -> contextInfo.setQuotedMessageChat((Chat) quotedMessageChat));
    }

    private CompletableFuture<Void> handleStoreFile(Store store, Path entry) {
        CompletableFuture var10000;
        switch (ProtobufControllerSerializer.FileType.of(entry).ordinal()) {
            case 0 -> var10000 = null;
            case 1 -> var10000 = CompletableFuture.runAsync(() -> {
                this.deserializeChat(store, entry);
            }).exceptionallyAsync(this::onError);
            case 2 -> var10000 = CompletableFuture.runAsync(() -> {
                this.deserializeNewsletter(store, entry);
            }).exceptionallyAsync(this::onError);
            default -> throw new MatchException((String)null, (Throwable)null);
        }

        return var10000;
    }

    public void deleteSession(Controller<?> controller) {
        IOException exception;
        PhoneNumber phoneNumber;
        try {
            Path folderPath = this.getSessionDirectory(controller.clientType(), controller.uuid().toString());
            this.delete(folderPath);
            phoneNumber = (PhoneNumber)controller.phoneNumber().orElse((PhoneNumber) null);
            if (phoneNumber == null) {
                return;
            }
        } catch (IOException var6) {
            exception = var6;
            throw new UncheckedIOException("Cannot delete session", exception);
        }

        try {
            Path linkedFolderPath = this.getSessionDirectory(controller.clientType(), phoneNumber.toString());
            Files.deleteIfExists(linkedFolderPath);
        } catch (IOException var5) {
            exception = var5;
            throw new UncheckedIOException("Cannot delete session", exception);
        }
    }

    private void delete(Path path) throws IOException {
        if (!Files.notExists(path, new LinkOption[0])) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public void linkMetadata(Controller<?> controller) {
        controller.phoneNumber().ifPresent((phoneNumber) -> {
            this.linkToUuid(controller.clientType(), controller.uuid(), phoneNumber.toString());
        });
        controller.alias().forEach((alias) -> {
            this.linkToUuid(controller.clientType(), controller.uuid(), alias);
        });
    }

    private void linkToUuid(ClientType type, UUID uuid, String string) {
        try {
            Path link = this.getSessionDirectory(type, string);
            Files.writeString(link, uuid.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException var5) {
        }

    }

    private void deserializeChat(Store store, Path chatFile) {
        try {
            GZIPInputStream input = new GZIPInputStream(Files.newInputStream(chatFile));

            try {
                Chat chat = ChatSpec.decode(input.readAllBytes());
                Iterator var5 = chat.messages().iterator();

                while(true) {
                    if (!var5.hasNext()) {
                        store.addChatDirect(chat);
                        break;
                    }

                    HistorySyncMessage message = (HistorySyncMessage)var5.next();
                    message.messageInfo().setChat(chat);
                }
            } catch (Throwable var8) {
                try {
                    input.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }

                throw var8;
            }

            input.close();
        } catch (IOException var9) {
            store.addChatDirect(this.rescueChat(chatFile));
        }

    }

    private Chat rescueChat(Path entry) {
        try {
            Files.deleteIfExists(entry);
        } catch (IOException var3) {
        }

        String chatName = entry.getFileName().toString().replaceFirst("chat_", "").replace(".proto", "");
        return (new ChatBuilder()).jid(Jid.of(chatName)).build();
    }

    private void deserializeNewsletter(Store store, Path newsletterFile) {
        try {
            GZIPInputStream input = new GZIPInputStream(Files.newInputStream(newsletterFile));

            try {
                Newsletter newsletter = NewsletterSpec.decode(input.readAllBytes());
                Iterator var5 = newsletter.messages().iterator();

                while(true) {
                    if (!var5.hasNext()) {
                        store.addNewsletter(newsletter);
                        break;
                    }

                    NewsletterMessageInfo message = (NewsletterMessageInfo)var5.next();
                    message.setNewsletter(newsletter);
                }
            } catch (Throwable var8) {
                try {
                    input.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }

                throw var8;
            }

            input.close();
        } catch (IOException var9) {
            store.addNewsletter(this.rescueNewsletter(newsletterFile));
        }

    }

    private Newsletter rescueNewsletter(Path entry) {
        try {
            Files.deleteIfExists(entry);
        } catch (IOException var3) {
        }

        String newsletterName = entry.getFileName().toString().replaceFirst("chat_", "").replace(".proto", "");
        return new Newsletter(Jid.of(newsletterName), (NewsletterState)null, (NewsletterMetadata)null, (NewsletterViewerMetadata)null);
    }

    private Path getHome(ClientType type) {
        return this.baseDirectory.resolve(type == ClientType.MOBILE ? "mobile" : "web");
    }

    private Path getSessionDirectory(ClientType clientType, String path) {
        try {
            Path result = this.getHome(clientType).resolve(path);
            Files.createDirectories(result.getParent());
            return result;
        } catch (IOException var4) {
            IOException exception = var4;
            throw new UncheckedIOException(exception);
        }
    }

    private Path getSessionFile(Store store, String fileName) {
        try {
            Path result = this.getSessionFile(store.clientType(), store.uuid().toString(), fileName);
            Files.createDirectories(result.getParent());
            return result;
        } catch (IOException var4) {
            IOException exception = var4;
            throw new UncheckedIOException("Cannot create directory", exception);
        }
    }

    private Path getSessionFile(ClientType clientType, String uuid, String fileName) {
        try {
            Path result = this.getSessionDirectory(clientType, uuid).resolve(fileName);
            Files.createDirectories(result.getParent());
            return result;
        } catch (IOException var5) {
            IOException exception = var5;
            throw new UncheckedIOException("Cannot create directory", exception);
        }
    }

    static {
        serializers.put(DEFAULT_SERIALIZER_PATH, new ProtobufControllerSerializer(DEFAULT_SERIALIZER_PATH));
    }

    private static class ImmutableLinkedList<E> extends LinkedList<E> {
        private static final ProtobufControllerSerializer.ImmutableLinkedList EMPTY = new ProtobufControllerSerializer.ImmutableLinkedList(new LinkedList());
        private final LinkedList<E> delegate;

        private static <E> ProtobufControllerSerializer.ImmutableLinkedList<E> empty() {
            return EMPTY;
        }

        private ImmutableLinkedList(LinkedList<E> delegate) {
            this.delegate = delegate;
        }

        public E getFirst() {
            return this.delegate.getFirst();
        }

        public E getLast() {
            return this.delegate.getLast();
        }

        public boolean contains(Object o) {
            return this.delegate.contains(o);
        }

        public int size() {
            return this.delegate.size();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public E get(int index) {
            return this.delegate.get(index);
        }

        public int indexOf(Object o) {
            return this.delegate.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return this.delegate.lastIndexOf(o);
        }

        public E peek() {
            return this.delegate.peek();
        }

        public E element() {
            return this.delegate.element();
        }

        public E poll() {
            return this.delegate.poll();
        }

        public boolean offer(E e) {
            return this.delegate.offer(e);
        }

        public boolean offerFirst(E e) {
            return this.delegate.offerFirst(e);
        }

        public boolean offerLast(E e) {
            return this.delegate.offerLast(e);
        }

        public E peekFirst() {
            return this.delegate.peekFirst();
        }

        public E peekLast() {
            return this.delegate.peekLast();
        }

        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        public void push(E e) {
            this.delegate.push(e);
        }

        public E pop() {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator(int index) {
            return this.delegate.listIterator(index);
        }

        public Iterator<E> descendingIterator() {
            return this.delegate.descendingIterator();
        }

        public Object clone() {
            return this.delegate.clone();
        }

        public Object[] toArray() {
            return this.delegate.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return this.delegate.toArray(a);
        }

        public Spliterator<E> spliterator() {
            return this.delegate.spliterator();
        }

        public LinkedList<E> reversed() {
            return this.delegate.reversed();
        }

        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }

        public void sort(Comparator<? super E> c) {
            this.delegate.sort(c);
        }

        public <T> T[] toArray(IntFunction<T[]> generator) {
            return this.delegate.toArray(generator);
        }

        public Stream<E> stream() {
            return this.delegate.stream();
        }

        public Stream<E> parallelStream() {
            return this.delegate.parallelStream();
        }

        public void forEach(Consumer<? super E> action) {
            this.delegate.forEach(action);
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }

        public void addLast(E e) {
            throw new UnsupportedOperationException();
        }

        public void addFirst(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public E remove() {
            throw new UnsupportedOperationException();
        }

        public E removeFirst() {
            throw new UnsupportedOperationException();
        }

        public E removeLast() {
            throw new UnsupportedOperationException();
        }

        protected void removeRange(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeFirstOccurrence(Object o) {
            throw new UnsupportedOperationException();
        }

        public E remove(int index) {
            throw new UnsupportedOperationException();
        }

        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }

        public boolean removeLastOccurrence(Object o) {
            throw new UnsupportedOperationException();
        }

        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }
    }

    private static enum FileType {
        UNKNOWN((String)null),
        CHAT("chat_"),
        NEWSLETTER("newsletter_");

        private final String prefix;

        private FileType(String prefix) {
            this.prefix = prefix;
        }

        private static ProtobufControllerSerializer.FileType of(Path path) {
            return (ProtobufControllerSerializer.FileType)Arrays.stream(values()).filter((entry) -> {
                return entry.prefix() != null && path.getFileName().toString().startsWith(entry.prefix());
            }).findFirst().orElse(UNKNOWN);
        }

        private String prefix() {
            return this.prefix;
        }
    }
}
