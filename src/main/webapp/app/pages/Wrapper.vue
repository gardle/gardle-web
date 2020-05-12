<template>
    <router-view></router-view>
</template>

<script lang="ts">
    import {Component} from 'vue-property-decorator';
    import Vue from 'vue';

    @Component
    export default class PageWrapper extends Vue {
    }
</script>
