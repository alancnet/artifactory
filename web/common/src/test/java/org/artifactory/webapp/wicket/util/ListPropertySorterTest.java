/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.webapp.wicket.util;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the CollectionPropertySorter.
 *
 * @author Yossi Shaul
 */
@Test
public class ListPropertySorterTest {
    private Person yossi;
    private Person ariel;

    @BeforeClass
    public void createPersons() {
        yossi = new Person("Yossi", "Shaul", 18);
        ariel = new Person("Ariel", "Shaul", 22);
    }

    @DataProvider(name = "default-persons")
    public Object[][] createData() {
        List<Person> persons = Arrays.asList(ariel, yossi);
        return new Object[][]{{persons}};
    }

    @Test(dataProvider = "default-persons")
    public void ascendingAndDescendingIntProperty(List<Person> persons) {
        SortParam ascendingAge = new SortParam("age", true);
        ListPropertySorter.sort(persons, ascendingAge);
        Assert.assertEquals(persons.size(), 2, "Sanity");
        Assert.assertEquals(persons.get(0), yossi);
        Assert.assertEquals(persons.get(1), ariel);

        SortParam descendingAge = new SortParam("age", false);
        ListPropertySorter.sort(persons, descendingAge);
        Assert.assertEquals(persons.get(0), ariel);
        Assert.assertEquals(persons.get(1), yossi);
    }

    @Test(dataProvider = "default-persons")
    public void ascendingAndDescendingStringProperty(List<Person> persons) {
        SortParam ascendingAge = new SortParam("firstName", true);
        ListPropertySorter.sort(persons, ascendingAge);
        Assert.assertEquals(persons.size(), 2, "Sanity");
        Assert.assertEquals(persons.get(0), ariel);
        Assert.assertEquals(persons.get(1), yossi);

        SortParam descendingAge = new SortParam("firstName", false);
        ListPropertySorter.sort(persons, descendingAge);
        Assert.assertEquals(persons.get(0), yossi);
        Assert.assertEquals(persons.get(1), ariel);
    }

    @Test(enabled = false, dataProvider = "default-persons")
    public void nonExistentProperty(List<Person> persons) {
        List<Person> personsCopy = new ArrayList<Person>(persons);

        SortParam noSuchPropertyAsc = new SortParam("blabla", true);
        ListPropertySorter.sort(persons, noSuchPropertyAsc);
        Assert.assertEquals(persons, personsCopy, "Sort by invalis peroperty should not change the order");

        SortParam noSuchPropertyDesc = new SortParam("blabla", false);
        ListPropertySorter.sort(persons, noSuchPropertyDesc);
        Assert.assertEquals(persons, personsCopy, "Sort by invalis peroperty should not change the order");
    }

    @Test(dataProvider = "default-persons")
    public void nullSortParam(List<Person> persons) {
        List<Person> personsCopy = new ArrayList<Person>(persons);

        ListPropertySorter.sort(persons, (SortParam) null);
        Assert.assertEquals(persons, personsCopy, "Null sort param should not change the order");
    }

    @Test
    public void stringPropertiesAreNotCaseSensitive() {
        Person person1 = new Person("Kooki", "B", 0);
        Person person2 = new Person("Kooki", "a", 0);
        List<Person> persons = Arrays.asList(person1, person2);

        SortParam lastNameAscending = new SortParam("lastName", false);
        ListPropertySorter.sort(persons, lastNameAscending);
        Assert.assertEquals(persons.get(0), person1, "in case insensitive sort 'a' is before 'B'");
    }

    // test class.

    private class Person {
        private String firstName;
        private String lastName;
        private int age;

        private Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Person person = (Person) obj;

            if (getAge() != person.getAge()) {
                return false;
            }
            if (!firstName.equals(person.firstName)) {
                return false;
            }
            if (getLastName() != null ? !getLastName().equals(person.getLastName()) : person.getLastName() != null) {
                return false;
            }

            return true;
        }

        public String toString() {
            return String.format("%s %s %s", firstName, getLastName(), getAge());
        }
    }

}
