package net.sourceforge.stripes.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public class GenericType {

   public static <T> Class<T> resolve( Class<?> clazz ) {
      return resolve(clazz, 0);
   }

   @SuppressWarnings("unchecked")
   public static <T> Class<T> resolve( Type type, Class<?> clazz, int index ) {
      Type[] genericInterfaces = clazz.getGenericInterfaces();

      for ( Type genericInterface : genericInterfaces ) {
         if ( ParameterizedType.class.isAssignableFrom(genericInterface.getClass()) ) {
            ParameterizedType parameterizedType = (ParameterizedType)genericInterface;
            if ( parameterizedType.getRawType() == type ) {
               return (Class<T>)(parameterizedType.getActualTypeArguments()[index]);
            }
         }
      }

      Type genericSuperclass = clazz.getGenericSuperclass();
      if ( genericSuperclass instanceof ParameterizedType ) {
         ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
         //noinspection unchecked
         return (Class<T>)(parameterizedType.getActualTypeArguments()[index]);
      } else if ( genericSuperclass instanceof Class<?> ) {
         return resolve(type, (Class<?>)genericSuperclass, index);
      }

      return null;
   }

   public static <T> Class<T> resolve( Class<?> clazz, int typeIndex ) {
      while ( clazz != null ) {
         Type parent = clazz.getGenericSuperclass();
         if ( parent instanceof ParameterizedType ) {
            return extractClass(((ParameterizedType)parent).getActualTypeArguments()[typeIndex]);
         }
         clazz = (Class<?>)parent;
      }

      return null;
   }

   @SuppressWarnings("unchecked")
   private static <T> Class<T> extractClass( Type type ) {
      if ( type instanceof ParameterizedType ) {
         return extractClass(((ParameterizedType)type).getRawType());
      }
      return (Class<T>)type;
   }
}
