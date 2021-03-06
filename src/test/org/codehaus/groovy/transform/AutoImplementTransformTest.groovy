/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.transform

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class AutoImplementTransformTest {

    @Test
    void testException() {
        shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException)
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testExceptionWithMessage() {
        def err = shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException, message='Not supported by Foo')
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
        assert err.message.contains('Not supported by Foo')
    }

    @Test
    void testClosureBody() {
        shouldFail IllegalStateException, '''
            import groovy.transform.*

            @AutoImplement(code={ throw new IllegalStateException() })
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    @Test
    void testInheritedMethodNotOverwritten() {
        assertScript '''
            class WithNext {
                String next() { 'foo' }
            }

            @groovy.transform.AutoImplement
            class Foo extends WithNext implements Iterator<String> { }
            assert new Foo().next() == 'foo'
        '''
    }

    @Test
    void testExistingMethodNotOverwritten() {
        assertScript '''
            @groovy.transform.AutoImplement
            class Foo implements Iterator<String> {
                String next() { 'foo' }
            }

            assert new Foo().next() == 'foo'
        '''
    }

    @NotYetImplemented @Test // GROOVY-9816
    void testPropertyMethodsNotOverwritten() {
        assertScript '''
            interface Bar {
              def getBaz(); void setBaz(baz)
            }

            @groovy.transform.AutoImplement
            class Foo implements Bar {
              def baz
            }

            def foo = new Foo(baz: 123)
            assert foo.baz == 123
        '''
    }

    @Test
    void testGenericReturnTypes() {
        assertScript '''
            interface HasXs<T> {
                T[] x()
            }

            abstract class HasXsY<E> implements HasXs<Long> {
                abstract E y()
            }

            interface MyIt<T> extends Iterator<T> { }

            @groovy.transform.AutoImplement
            class Foo extends HasXsY<Integer> implements MyIt<String> { }

            def publicMethods = Foo.methods.findAll{ it.modifiers == 1 }.collect{ "$it.returnType.simpleName $it.name" }*.toString()
            assert ['boolean hasNext', 'String next', 'Long[] x', 'Integer y'].every{ publicMethods.contains(it) }
            '''
    }

    @NotYetImplemented @Test // GROOVY-8270
    void testCovariantMethodImpl() {
        assertScript '''
            @groovy.transform.AutoImplement
            class Foo implements Comparator<String> { }
            // Can't have an abstract method in a non-abstract class. The class 'Foo' must be declared
            // abstract or the method 'int compare(java.lang.Object, java.lang.Object)' must be implemented.

            def foo = new Foo()
            assert (foo <=> 'foo') == 0
            assert (foo.compareTo('foo')) == 0
            '''
    }
}
