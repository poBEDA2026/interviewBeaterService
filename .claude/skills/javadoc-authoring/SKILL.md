---
name: javadoc-authoring
description: Use when creating a new Java method, constructor, or class, or when modifying the signature of an existing one in a Spring/JPA/Lombok project — enforces Russian-language JavaDoc with required @param/@return/@throws tags, {@link} cross-references, and a no-trailing-period summary line
---

# Javadoc Authoring

## Overview

In this project, every public/protected method, constructor, and class carries a Russian-language JavaDoc block that documents its purpose, parameters, return value, and thrown exceptions. The block is the single source of truth for IDE hover-docs and generated reference. This skill encodes the exact shape of that block so every new or modified symbol gets the same documentation on the first edit — no follow-up cleanup pass required.

**Violating the letter of these rules is violating the spirit of these rules.** "Close enough" JavaDoc creates inconsistent hover-docs, broken `{@link}` resolution, and forces a second sweep over the file.

## When to Use

Use this skill whenever a request to the model results in any of the following in a Java source file under `src/main/java/`:

- Adding a new public/protected method or constructor.
- Adding a new public class, interface, record, or enum.
- Changing the signature of an existing method (rename, add/remove/retype a parameter, change return type, add/remove a checked or documented unchecked exception, change visibility from private/package to public/protected).
- Adding a documented field that exposes behaviour (e.g. a `static final` constant with non-obvious meaning, or a public field on a DTO).
- Modifying any public class that currently has no class-level JavaDoc. The first edit to such a class is the right moment to add one — do not leave it for a later pass.

Do **not** use it for:

- Lombok-generated methods (`getX`/`setX`/`builder`/`equals`/`hashCode`/`toString`) — they have no body to document.
- Methods whose only body is a delegation whose JavaDoc is identical to the delegate's.
- Test code (style is enforced separately, if at all).
- Renaming a method without changing its signature, parameters, return type, or throws — keep the existing JavaDoc as-is.

## Triggers That Mean Re-Read This Skill

Stop and re-read the rules below before responding if any of these are true:

- The method has a non-`void` return type and the new JavaDoc has no `@return`.
- The method takes parameters and the new JavaDoc has no `@param` entries.
- The method declares `throws` (or any runtime exception by convention) and the new JavaDoc has no `@throws`.
- The summary line ends with a period.
- The block is in English while the rest of the file is in Russian.
- The block uses no `{@link}` while referencing a project type.

## The Required Block Shape

A compliant method JavaDoc has **exactly this structure**, in this order, with no extra prose between sections:

```java
/**
 * <summary line on a single line, no trailing period>
 *
 * @param <name> <one-line description in Russian>
 * @return <one-line description, with {@link TypeName} if returning a project type>
 * @throws <FullyQualifiedOrImportedType> <condition under which it is thrown>
 */
```

Rules for each part:

- **Summary line.** One line. Imperative-style noun phrase (matches existing style: "Получение вопроса по идентификатору", "Создание нового вопроса"). No trailing period. No leading article.
- **Blank `*` line** between summary and tags. Never omit it — the javadoc tool requires it for tag separation.
- **`@param`.** One entry per parameter, in declaration order. Description is a short noun phrase ("идентификатор вопроса", "заголовок вопроса"), not a sentence. When the parameter type is a project type (not a JDK type), wrap it in `{@link …}` on its first mention: `идентификатор {@link Question}а` style. JDK types (`String`, `Long`, `List`, `Map`, etc.) are never linked.
- **`@return`.** Required for every non-`void` method. **Required** to use `{@link TypeName}` for the return type when it is a project type. Example: `@return объект типа {@link Question}`. JDK/primitive return types (`String`, `long`, `List<...>` of JDK types) are described in prose without `{@link}`.
- **`@throws`.** Required for every exception the method can propagate, including unchecked ones thrown explicitly. Description names the condition, not the type: `если вопрос с указанным идентификатором не найден`. **Required** to use `{@link …}` when the throw is a project type the reader will want to navigate to (e.g. `@throws {@link QuestionNotFoundException} если ...`).
- **Class-level JavaDoc.** Required on every public class. One or two lines describing the class's responsibility in the system. If the class is a Spring `@Configuration` or `@Service`, mention what it configures/provides; if it is a JPA entity, mention the table and the role of the entity.
- **Language.** Russian throughout. Do not mix English and Russian in the same block. Project type names and annotations stay in English.

## Quick Reference

| Symbol kind | JavaDoc required? | Required tags |
|---|---|---|
| Public/protected method | Yes | `@param` per param, `@return` if non-`void`, `@throws` per documented exception |
| Public constructor | Yes (when it does non-trivial work) | `@param` per param; no `@return` |
| Public class / interface / record / enum | Yes (class-level) | n/a |
| Private method | Optional — add only if logic is non-obvious | `@param`/`@return`/`@throws` matching public style |
| Lombok-generated (`@Getter`/etc.) | No | n/a |
| `@Override` method | No — inherit parent's JavaDoc | n/a |
| `static final` constant | Optional | one-line description if meaning is non-obvious |

## When Editing an Existing JavaDoc

When a method's signature changes, update the JavaDoc to match the **new** signature before considering the edit done:

- Parameter added → add `@param` for it.
- Parameter removed → delete its `@param` and re-number/renumber nothing (just delete the line).
- Return type changed → rewrite `@return`.
- `throws` added/removed → update `@throws` block.
- Method renamed → update the summary line so it describes the new name's behaviour; do not leave a stale name in the summary.

If only the body changed and the signature is identical, leave the JavaDoc alone.

## Common Mistakes

- **English summary in a Russian file.** The whole file must read in one language. If neighbouring methods are in Russian, new methods must be too.
- **Summary ending with a period.** `* Получение вопроса по идентификатору.` is wrong. Drop the period.
- **Missing `{@link}` on `@return`.** `@return объект типа {@link Question}` — always link the return type when it is a project type.
- **`@throws` with the type only, no condition.** `@throws QuestionNotFoundException` alone is wrong; add the triggering condition.
- **Adding a `@throws` for a caught-and-swallowed exception.** Only document exceptions that actually escape the method.
- **Omitting the blank `*` line** between summary and tags. The block will still parse, but it will not match project style and the javadoc tool's group separator will be missing.
- **Forgetting class-level JavaDoc on a new public class.** The IDE hover on the class will show package and nothing else.

## Red Flags — STOP and Fix

- New method has no JavaDoc at all.
- New method's JavaDoc is in English while the file is in Russian.
- Summary line ends with `.`, `!`, or `?`.
- `@return` references a project type without `{@link}`.
- `@throws` references a project-type exception without `{@link}`.
- A `throws` clause is present in the signature but the JavaDoc has no matching `@throws`.
- A class was added at public visibility and has no class-level JavaDoc.
- A class without class-level JavaDoc was edited and the class-level block is still missing after the edit.

**All of these mean: edit the file before declaring the task done. Do not leave a follow-up to a separate pass.**
