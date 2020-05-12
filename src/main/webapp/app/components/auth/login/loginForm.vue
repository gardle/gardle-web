<template>
    <v-form>
        <v-text-field
            v-model="username"
            :error-messages="usernameErrors"
            :label="$t('login.form.label.username')"
            outlined
            required
            @input="$v.username.$touch()"
            @blur="$v.username.$touch()"
            @keyup.enter="submit"
        ></v-text-field>
        <v-text-field
            v-model="password"
            :error-messages="passwordErrors"
            :label="$t('login.form.label.password')"
            outlined
            required
            type="password"
            @input="$v.password.$touch()"
            @blur="$v.password.$touch()"
            @keyup.enter="submit"
        ></v-text-field>

        <v-row justify="end">
            <v-col cols="auto">
                <v-btn text class="grey--text" @click="reset" v-text="$t('login.form.button.reset')">Reset</v-btn>
            </v-col>
            <v-col cols="auto">
                <v-btn depressed class="primary" @click="submit" v-text="$t('login.form.button.login')">Login</v-btn>
            </v-col>
        </v-row>
    </v-form>
</template>

<script lang="ts" src="./loginForm.component.ts">
</script>

<style scoped>

</style>
