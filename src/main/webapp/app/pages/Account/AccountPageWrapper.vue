<template>
    <v-row class="fill-height" justify="center" no-gutters>
        <portal to="navigationDrawerContent">
            <v-list class="pt-0">
                <template v-for="(page, i) in pages">
                    <v-list-item :key="i" :to="page.to" :two-line="page.twoLine" :class="[(page.classes) ? page.classes : '']" :active-class="page_active_class">
                        <v-list-item-avatar class="my-0" icon>
                            <v-icon>{{page.icon}}</v-icon>
                        </v-list-item-avatar>
                        <v-list-item-content>
                            <v-list-item-title>{{page.key ? $t(page.key) : page.hasUserName ? userName : page.name }}</v-list-item-title>
                            <v-list-item-subtitle v-if="page.twoLine && page.twoLine === true">{{$t(page.subTitle)}}</v-list-item-subtitle>
                        </v-list-item-content>
                    </v-list-item>
                </template>
            </v-list>
        </portal>
        <v-col cols="12">
            <v-container fluid class="fill-height pa-0 align-start">
                <router-view></router-view>
            </v-container>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./AccountPageWrapper.component.ts">
</script>

<style scoped>

</style>
