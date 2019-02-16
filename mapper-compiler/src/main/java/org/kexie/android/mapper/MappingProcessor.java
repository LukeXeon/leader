package org.kexie.android.mapper;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public final class MappingProcessor
        extends AbstractProcessor
{
    private static final String sPackageName
            = "org.kexie.android.mapper.internal";

    private static final String[] sFragmentTypes = {
            "android.support.v4.app.Fragment",
            "androidx.fragment.app.Fragment"
    };

    private static final String[] sKeepAnnotationTypes = {
            "androidx.annotation.Keep",
            "android.support.annotation.Keep"
    };

    private TypeElement mKeepAnnotationType;
    private TypeElement mFragmentType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
        mKeepAnnotationType = getAndroidType(sKeepAnnotationTypes);
        mFragmentType = getAndroidType(sFragmentTypes);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set,
                           RoundEnvironment roundEnvironment)
    {
        Set<? extends Element> mappingElements = roundEnvironment
                .getElementsAnnotatedWith(Mapping.class);
        if (mappingElements.isEmpty())
        {
            return false;
        }

        Map<URI, TypeName> mappings = new HashMap<>();
        mappingElements.stream()
                .filter((Predicate<Element>) element ->
                        processingEnv.getTypeUtils().isAssignable(
                                element.asType(),
                                mFragmentType.asType()
                        ) && !element.getModifiers()
                                .contains(Modifier.ABSTRACT))
                .forEach((Consumer<Element>) element -> {
                    URI uri = URI.create(element
                            .getAnnotation(Mapping.class).value());
                    if (mappings.put(uri, TypeName.get(element.asType())) != null)
                    {
                        throw new RuntimeException(
                                String.format("URI重复%s", uri));
                    }
                });
        mappings.entrySet()
                .stream()
                .map(entry -> TypeSpec.classBuilder("Entry_"
                        + hash(entry.getKey()))
                        .addModifiers(Modifier.FINAL)
                        .addSuperinterface(ClassName
                                .bestGuess("org.kexie.android.mapper.Entry"))
                        .addAnnotation(AnnotationSpec
                                .builder(ClassName.get(mKeepAnnotationType))
                                .build())
                        .addAnnotation(AnnotationSpec.builder(Mapping.class)
                                .addMember("value",
                                        "$S",
                                        entry.getKey().toString())
                                .build())
                        .addMethod(getKeyOverride(entry.getKey()))
                        .addMethod(getValueOverride(entry.getValue()))
                        .build())
                .forEach(typeSpec -> {
                    try
                    {
                        JavaFile.builder(sPackageName, typeSpec)
                                .build()
                                .writeTo(processingEnv.getFiler());
                    } catch (IOException e)
                    {
                        throw new RuntimeException("URI重复或文件写入失败");
                    }
                });
        return false;
    }

    private MethodSpec getKeyOverride(URI uri)
    {
        return MethodSpec.methodBuilder("getKey")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement("return $S", uri.toString())
                .build();
    }

    private MethodSpec getValueOverride(TypeName typeName)
    {
        return MethodSpec.methodBuilder("getValue")
                .addModifiers(Modifier.PUBLIC)
                .returns(Class.class)
                .addAnnotation(Override.class)
                .addStatement("return $T.class", typeName)
                .build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return Collections.singleton(Mapping.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    private TypeElement getAndroidType(String[] typeNames)
    {
        for (String typeName : typeNames)
        {
            TypeElement typeElement = getType(typeName);
            if (typeElement != null)
            {
                return typeElement;
            }
        }
        throw new RuntimeException(String.format("找不到%s",
                Arrays.toString(typeNames)));
    }

    //线性探索法hash
    private long hash(URI uri)
    {
        long hashCode = Integer.toUnsignedLong(uri.hashCode());
        while (true)
        {
            TypeElement typeElement = getType(sPackageName
                    + "Entry_"
                    + hashCode);
            if (typeElement == null)
            {
                return hashCode;
            }
            if (uri.equals(URI.create(typeElement
                    .getAnnotation(Mapping.class).value())))
            {
                throw new RuntimeException(
                        String.format("URI重复%s", uri));
            } else
            {
                hashCode++;
            }
        }
    }

    private TypeElement getType(CharSequence charSequence)
    {
        return processingEnv.getElementUtils()
                .getTypeElement(charSequence);
    }
}
